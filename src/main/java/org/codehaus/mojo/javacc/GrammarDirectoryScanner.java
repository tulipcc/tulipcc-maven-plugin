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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Scans source directories for JavaCC grammar files.
 *
 * @author Benjamin Bentmann
 * @version $Id: GrammarDirectoryScanner.java 6282 2008-02-09 23:49:06Z bentmann
 *          $
 */
class GrammarDirectoryScanner
{

  /**
   * The directory scanner used to scan the source directory for files.
   */
  private final DirectoryScanner scanner;

  /**
   * The absolute path to the output directory used to detect stale target files
   * by timestamp checking, may be <code>null</code> if no stale detection
   * should be performed.
   */
  private File outputDirectory;

  // TODO: Once the parameter "packageName" from the javacc mojo has been
  // deleted, remove this field, too.
  /**
   * The package name for the generated parser, may be <code>null</code> to use
   * the package declaration from the grammar file.
   */
  private String parserPackage;

  /**
   * The granularity in milliseconds of the last modification date for testing
   * whether a grammar file needs recompilation because its corresponding target
   * file is stale.
   */
  private int staleMillis;

  /**
   * A set of grammar infos describing the included grammar files, must never be
   * <code>null</code>.
   */
  private final List <GrammarInfo> includedGrammars;

  /**
   * Creates a new grammar directory scanner.
   */
  public GrammarDirectoryScanner ()
  {
    this.scanner = new DirectoryScanner ();
    this.scanner.setFollowSymlinks (true);
    this.includedGrammars = new ArrayList <> ();
  }

  /**
   * Sets the absolute path to the source directory to scan for grammar files.
   * This directory must exist or the scanner will report an error.
   *
   * @param directory
   *        The absolute path to the source directory to scan, must not be
   *        <code>null</code>.
   */
  public void setSourceDirectory (final File directory)
  {
    if (!directory.isAbsolute ())
    {
      throw new IllegalArgumentException ("source directory is not absolute: " + directory);
    }
    this.scanner.setBasedir (directory);
  }

  /**
   * Sets the package name for the generated parser.
   *
   * @param packageName
   *        The package name for the generated parser, may be <code>null</code>
   *        to use the package declaration from the grammar file.
   */
  public void setParserPackage (final String packageName)
  {
    this.parserPackage = packageName;
  }

  /**
   * Sets the Ant-like inclusion patterns.
   *
   * @param includes
   *        The set of Ant-like inclusion patterns, may be <code>null</code> to
   *        include all files.
   */
  public void setIncludes (final String [] includes)
  {
    this.scanner.setIncludes (includes);
  }

  /**
   * Sets the Ant-like exclusion patterns.
   *
   * @param excludes
   *        The set of Ant-like exclusion patterns, may be <code>null</code> to
   *        exclude no files.
   */
  public void setExcludes (final String [] excludes)
  {
    this.scanner.setExcludes (excludes);
    this.scanner.addDefaultExcludes ();
  }

  /**
   * Sets the absolute path to the output directory used to detect stale target
   * files.
   *
   * @param directory
   *        The absolute path to the output directory used to detect stale
   *        target files by timestamp checking, may be <code>null</code> if no
   *        stale detection should be performed.
   */
  public void setOutputDirectory (final File directory)
  {
    if (directory != null && !directory.isAbsolute ())
    {
      throw new IllegalArgumentException ("output directory is not absolute: " + directory);
    }
    this.outputDirectory = directory;
  }

  /**
   * Sets the granularity in milliseconds of the last modification date for
   * stale file detection.
   *
   * @param milliseconds
   *        The granularity in milliseconds of the last modification date for
   *        testing whether a grammar file needs recompilation because its
   *        corresponding target file is stale.
   */
  public void setStaleMillis (final int milliseconds)
  {
    this.staleMillis = milliseconds;
  }

  /**
   * Scans the source directory for grammar files that match at least one
   * inclusion pattern but no exclusion pattern, optionally performing timestamp
   * checking to exclude grammars whose corresponding parser files are up to
   * date.
   *
   * @throws IOException
   *         If a grammar file could not be analyzed for metadata.
   */
  public void scan () throws IOException
  {
    this.includedGrammars.clear ();
    this.scanner.scan ();

    final String [] includedFiles = this.scanner.getIncludedFiles ();
    for (final String includedFile : includedFiles)
    {
      final GrammarInfo grammarInfo = new GrammarInfo (this.scanner.getBasedir (), includedFile, this.parserPackage);
      if (this.outputDirectory != null)
      {
        final File sourceFile = grammarInfo.getGrammarFile ();
        final File [] targetFiles = getTargetFiles (this.outputDirectory, includedFile, grammarInfo);
        for (final File targetFile2 : targetFiles)
        {
          final File targetFile = targetFile2;
          if (!targetFile.exists () || targetFile.lastModified () + this.staleMillis < sourceFile.lastModified ())
          {
            this.includedGrammars.add (grammarInfo);
            break;
          }
        }
      }
      else
      {
        this.includedGrammars.add (grammarInfo);
      }
    }
  }

  /**
   * Determines the output files corresponding to the specified grammar file.
   *
   * @param targetDirectory
   *        The absolute path to the output directory for the target files, must
   *        not be <code>null</code>.
   * @param grammarFile
   *        The path to the grammar file, relative to the scanned source
   *        directory, must not be <code>null</code>.
   * @param grammarInfo
   *        The grammar info describing the grammar file, must not be
   *        <code>null</code>
   * @return A file array with target files, never <code>null</code>.
   */
  protected File [] getTargetFiles (final File targetDirectory, final String grammarFile, final GrammarInfo grammarInfo)
  {
    final File parserFile = new File (targetDirectory, grammarInfo.getParserFile ());
    return new File [] { parserFile };
  }

  /**
   * Gets the grammar files that were included by the scanner during the last
   * invocation of {@link #scan()}.
   *
   * @return An array of grammar infos describing the included grammar files,
   *         will be empty if no files were included but is never
   *         <code>null</code>.
   */
  public GrammarInfo [] getIncludedGrammars ()
  {
    return this.includedGrammars.toArray (new GrammarInfo [this.includedGrammars.size ()]);
  }

}
