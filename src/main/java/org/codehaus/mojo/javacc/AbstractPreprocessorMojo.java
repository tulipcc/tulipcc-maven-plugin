package org.codehaus.mojo.javacc;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.Arrays;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Provides common services for all mojos that preprocess JavaCC grammar files.
 *
 * @author Benjamin Bentmann
 * @version $Id: AbstractPreprocessorMojo.java 6353 2008-02-27 22:14:08Z
 *          bentmann $
 */
public abstract class AbstractPreprocessorMojo extends AbstractMojo
{
  /**
   * The current Maven project.
   *
   * @parameter default-value="${project}"
   * @readonly
   * @required
   */
  private MavenProject project;

  /**
   * Gets the absolute path to the directory where the grammar files are
   * located.
   *
   * @return The absolute path to the directory where the grammar files are
   *         located, never <code>null</code>.
   */
  protected abstract File getSourceDirectory ();

  /**
   * Gets a set of Ant-like inclusion patterns used to select files from the
   * source directory for processing.
   *
   * @return A set of Ant-like inclusion patterns used to select files from the
   *         source directory for processing, can be <code>null</code> if all
   *         files should be included.
   */
  protected abstract String [] getIncludes ();

  /**
   * Gets a set of Ant-like exclusion patterns used to unselect files from the
   * source directory for processing.
   *
   * @return A set of Ant-like inclusion patterns used to unselect files from
   *         the source directory for processing, can be <code>null</code> if no
   *         files should be excluded.
   */
  protected abstract String [] getExcludes ();

  /**
   * Gets the absolute path to the directory where the generated Java files for
   * the parser will be stored.
   *
   * @return The absolute path to the directory where the generated Java files
   *         for the parser will be stored, never <code>null</code>.
   */
  protected abstract File getOutputDirectory ();

  /**
   * Gets the absolute path to the directory where the processed input files
   * will be stored for later detection of stale sources.
   *
   * @return The absolute path to the directory where the processed input files
   *         will be stored for later detection of stale sources, never
   *         <code>null</code>.
   */
  protected abstract File getTimestampDirectory ();

  /**
   * Gets the granularity in milliseconds of the last modification date for
   * testing whether a source needs recompilation.
   *
   * @return The granularity in milliseconds of the last modification date for
   *         testing whether a source needs recompilation.
   */
  protected abstract int getStaleMillis ();

  /**
   * Execute the tool.
   *
   * @throws MojoExecutionException
   *         If the invocation of the tool failed.
   * @throws MojoFailureException
   *         If the tool reported a non-zero exit code.
   */
  public void execute () throws MojoExecutionException, MojoFailureException
  {
    if (false)
      getLog ().warn ("This goal has been deprecated. Please update your plugin configuration.");

    final GrammarInfo [] grammarInfos = scanForGrammars ();

    if (grammarInfos == null)
    {
      getLog ().info ("Skipping non-existing source directory: " + getSourceDirectory ());
      return;
    }
    else
      if (grammarInfos.length <= 0)
      {
        getLog ().info ("Skipping - all parsers are up to date");
      }
      else
      {
        if (!getTimestampDirectory ().exists ())
        {
          getTimestampDirectory ().mkdirs ();
        }

        for (final GrammarInfo grammarInfo : grammarInfos)
        {
          processGrammar (grammarInfo);
        }
        getLog ().info ("Processed " + grammarInfos.length + " grammar" + (grammarInfos.length != 1 ? "s" : ""));
      }

    addCompileSourceRoot ();
  }

  /**
   * Passes the specified grammar file through the tool.
   *
   * @param grammarInfo
   *        The grammar info describing the grammar file to process, must not be
   *        <code>null</code>.
   * @throws MojoExecutionException
   *         If the invocation of the tool failed.
   * @throws MojoFailureException
   *         If the tool reported a non-zero exit code.
   */
  protected abstract void processGrammar (GrammarInfo grammarInfo) throws MojoExecutionException, MojoFailureException;

  /**
   * Scans the configured source directory for grammar files which need
   * processing.
   *
   * @return An array of grammar infos describing the found grammar files or
   *         <code>null</code> if the source directory does not exist.
   * @throws MojoExecutionException
   *         If the source directory could not be scanned.
   */
  private GrammarInfo [] scanForGrammars () throws MojoExecutionException
  {
    if (!getSourceDirectory ().isDirectory ())
    {
      return null;
    }

    GrammarInfo [] grammarInfos;

    getLog ().debug ("Scanning for grammars: " + getSourceDirectory ());
    try
    {
      final GrammarDirectoryScanner scanner = new LegacyGrammarDirectoryScanner ();
      scanner.setSourceDirectory (getSourceDirectory ());
      scanner.setIncludes (getIncludes ());
      scanner.setExcludes (getExcludes ());
      scanner.setOutputDirectory (getTimestampDirectory ());
      scanner.setStaleMillis (getStaleMillis ());
      scanner.scan ();
      grammarInfos = scanner.getIncludedGrammars ();
    }
    catch (final Exception e)
    {
      throw new MojoExecutionException ("Failed to scan for grammars: " + getSourceDirectory (), e);
    }
    getLog ().debug ("Found grammars: " + Arrays.asList (grammarInfos));

    return grammarInfos;
  }

  /**
   * Creates the timestamp file for the specified grammar file.
   *
   * @param grammarInfo
   *        The grammar info describing the grammar file to process, must not be
   *        <code>null</code>.
   */
  protected void createTimestamp (final GrammarInfo grammarInfo)
  {
    final File jjFile = grammarInfo.getGrammarFile ();
    final File timestampFile = new File (getTimestampDirectory (), grammarInfo.getRelativeGrammarFile ());
    try
    {
      FileUtils.copyFile (jjFile, timestampFile);
    }
    catch (final Exception e)
    {
      getLog ().warn ("Failed to create copy for timestamp check: " + jjFile, e);
    }
  }

  /**
   * Registers the configured output directory as a compile source root for the
   * current project.
   */
  protected void addCompileSourceRoot ()
  {
    if (this.project != null)
    {
      getLog ().debug ("Adding compile source root: " + getOutputDirectory ());
      this.project.addCompileSourceRoot (getOutputDirectory ().getAbsolutePath ());
    }
  }

}
