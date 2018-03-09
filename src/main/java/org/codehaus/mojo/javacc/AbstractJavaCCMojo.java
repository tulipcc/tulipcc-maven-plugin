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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.SelectorUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Provides common services for all mojos that compile JavaCC grammar files.
 *
 * @author jruiz@exist.com
 * @author jesse <jesse.mcconnell@gmail.com>
 * @version $Id: AbstractJavaCCMojo.java 10774 2009-09-26 11:40:48Z bentmann $
 */
public abstract class AbstractJavaCCMojo extends AbstractMojo
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
   * The set of compile source roots whose contents are not generated as part of
   * the build, i.e. those that usually reside somewhere below "${basedir}/src"
   * in the project structure. Files in these source roots are owned by the user
   * and must not be overwritten with generated files.
   */
  private Collection <File> nonGeneratedSourceRoots;

  /**
   * The Java version for which to generate source code. Default value is
   * <code>1.5</code> for plugin version 2.6+ and <code>1.4</code> in older
   * versions.
   *
   * @parameter property=jdkVersion
   * @since 2.4
   */
  private String jdkVersion;

  /**
   * The number of tokens to look ahead before making a decision at a choice
   * point during parsing. The default value is <code>1</code>.
   *
   * @parameter property=lookAhead
   */
  private Integer lookAhead;

  /**
   * This is the number of tokens considered in checking choices of the form "A
   * | B | ..." for ambiguity. Default value is <code>2</code>.
   *
   * @parameter property=choiceAmbiguityCheck
   */
  private Integer choiceAmbiguityCheck;

  /**
   * This is the number of tokens considered in checking all other kinds of
   * choices (i.e., of the forms "(A)*", "(A)+", and "(A)?") for ambiguity.
   * Default value is <code>1</code>.
   *
   * @parameter property=otherAmbiguityCheck
   */
  private Integer otherAmbiguityCheck;

  /**
   * This option is used to obtain debugging information from the generated
   * parser. Setting this option to <code>true</code> causes the parser to
   * generate a trace of its actions. Default value is <code>false</code>.
   *
   * @parameter property=debugParser
   */
  private Boolean debugParser;

  /**
   * This is a boolean option whose default value is <code>false</code>. Setting
   * this option to <code>true</code> causes the parser to generate all the
   * tracing information it does when the option <code>debugParser</code> is
   * <code>true</code>, and in addition, also causes it to generated a trace of
   * actions performed during lookahead operation.
   *
   * @parameter property=debugLookAhead
   */
  private Boolean debugLookAhead;

  /**
   * This option is used to obtain debugging information from the generated
   * token manager. Default value is <code>false</code>.
   *
   * @parameter property=debugTokenManager
   */
  private Boolean debugTokenManager;

  /**
   * Setting it to <code>false</code> causes errors due to parse errors to be
   * reported in somewhat less detail. Default value is <code>true</code>.
   *
   * @parameter property=errorReporting
   */
  private Boolean errorReporting;

  /**
   * When set to <code>true</code>, the generated parser uses an input stream
   * object that processes Java Unicode escapes (<code>\</code><code>u</code>
   * <i>xxxx</i>) before sending characters to the token manager. Default value
   * is <code>false</code>.
   *
   * @parameter property=javaUnicodeEscape
   */
  private Boolean javaUnicodeEscape;

  /**
   * When set to <code>true</code>, the generated parser uses uses an input
   * stream object that reads Unicode files. By default, ASCII files are
   * assumed. Default value is <code>false</code>.
   *
   * @parameter property=unicodeInput
   */
  private Boolean unicodeInput;

  /**
   * Setting this option to <code>true</code> causes the generated token manager
   * to ignore case in the token specifications and the input files. Default
   * value is <code>false</code>.
   *
   * @parameter property=ignoreCase
   */
  private Boolean ignoreCase;

  /**
   * When set to <code>true</code>, every call to the token manager's method
   * <code>getNextToken()</code> (see the description of the
   * <a href="https://javacc.dev.java.net/doc/apiroutines.html">Java Compiler
   * Compiler API</a>) will cause a call to a user-defined method
   * <code>CommonTokenAction()</code> after the token has been scanned in by the
   * token manager. Default value is <code>false</code>.
   *
   * @parameter property=commonTokenAction
   */
  private Boolean commonTokenAction;

  /**
   * The default action is to generate a token manager that works on the
   * specified grammar tokens. If this option is set to <code>true</code>, then
   * the parser is generated to accept tokens from any token manager of type
   * <code>TokenManager</code> - this interface is generated into the generated
   * parser directory. Default value is <code>false</code>.
   *
   * @parameter property=userTokenManager
   */
  private Boolean userTokenManager;

  /**
   * This flag controls whether the token manager will read characters from a
   * character stream reader as defined by the options
   * <code>javaUnicodeEscape</code> and <code>unicodeInput</code> or whether the
   * token manager reads from a user-supplied implementation of
   * <code>CharStream</code>. Default value is <code>false</code>.
   *
   * @parameter property=userCharStream
   */
  private Boolean userCharStream;

  /**
   * A flag that controls whether the parser file (<code>*Parser.java</code>)
   * should be generated or not. If set to <code>false</code>, only the token
   * manager is generated. Default value is <code>true</code>.
   *
   * @parameter property=buildParser
   */
  private Boolean buildParser;

  /**
   * A flag that controls whether the token manager file (
   * <code>*TokenManager.java</code>) should be generated or not. Setting this
   * to <code>false</code> can speed up the generation process if only the
   * parser part of the grammar changed. Default value is <code>true</code>.
   *
   * @parameter property=buildTokenManager
   */
  private Boolean buildTokenManager;

  /**
   * When set to <code>true</code>, the generated token manager will include a
   * field called <code>parser</code> that references the instantiating parser
   * instance. Default value is <code>false</code>.
   *
   * @parameter property=tokenManagerUsesParser
   */
  private Boolean tokenManagerUsesParser;

  /**
   * The name of the base class for the generated <code>Token</code> class.
   * Default value is <code>java.lang.Object</code>.
   *
   * @parameter property=tokenExtends
   * @since 2.5
   */
  private String tokenExtends;

  /**
   * The name of a custom factory class used to create <code>Token</code>
   * objects. This class must have a method with the signature
   * <code>public static Token newToken(int ofKind, String image)</code>. By
   * default, tokens are created by calling <code>Token.newToken()</code>.
   *
   * @parameter property=tokenFactory
   * @since 2.5
   */
  private String tokenFactory;

  /**
   * Enables/disables many syntactic and semantic checks on the grammar file
   * during parser generation. Default value is <code>true</code>.
   *
   * @parameter property=sanityCheck
   */
  private Boolean sanityCheck;

  /**
   * This option setting controls lookahead ambiguity checking performed by
   * JavaCC. Default value is <code>false</code>.
   *
   * @parameter property=forceLaCheck
   */
  private Boolean forceLaCheck;

  /**
   * Setting this option to <code>true</code> causes the generated parser to
   * lookahead for extra tokens ahead of time. Default value is
   * <code>false</code>.
   *
   * @parameter property=cacheTokens
   */
  private Boolean cacheTokens;

  /**
   * A flag whether to keep line and column information along with a token.
   * Default value is <code>true</code>.
   *
   * @parameter property=keepLineColumn
   */
  private Boolean keepLineColumn;

  /**
   * A flag whether the generated support classes of the parser should have
   * public or package-private visibility. Default value is <code>true</code>.
   *
   * @parameter property=supportClassVisibilityPublic
   * @since 2.6
   */
  private Boolean supportClassVisibilityPublic;

  /**
   * The file encoding to use for reading the grammar files.
   *
   * @parameter property=grammarEncoding
   *            default-value="${project.build.sourceEncoding}"
   * @since 2.6
   */
  private String grammarEncoding;

  /**
   * The file encoding to use for writing the output files.
   *
   * @parameter property=outputEncoding
   *            default-value="${project.build.sourceEncoding}"
   * @since 4.1.0
   */
  private String outputEncoding;

  /**
   * The Java template type to be used. Must be "modern" or "classic". Defaults
   * to "classic".
   *
   * @parameter default="classic"
   * @since 4.1.0
   */
  private String javaTemplateType;

  /**
   * Gets the file encoding of the grammar files.
   *
   * @return The file encoding of the grammar files or <code>null</code> if the
   *         user did not specify this mojo parameter.
   */
  protected String getGrammarEncoding ()
  {
    return this.grammarEncoding;
  }

  /**
   * Gets the file encoding of the output files.
   *
   * @return The file encoding of the output files or <code>null</code> if the
   *         user did not specify this mojo parameter.
   */
  protected String getOutputEncoding ()
  {
    return this.outputEncoding;
  }

  /**
   * Gets the Java version for which to generate source code.
   *
   * @return The Java version for which to generate source code, will be
   *         <code>null</code> if the user did not specify this mojo parameter.
   */
  protected String getJdkVersion ()
  {
    return this.jdkVersion;
  }

  /**
   * Gets the Java template type to be used.
   *
   * @return The java template type or <code>null</code> if the user did not
   *         specify this mojo parameter.
   */
  protected String getJavaTemplateType ()
  {
    return this.javaTemplateType;
  }

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
   * Gets the granularity in milliseconds of the last modification date for
   * testing whether a source needs recompilation.
   *
   * @return The granularity in milliseconds of the last modification date for
   *         testing whether a source needs recompilation.
   */
  protected abstract int getStaleMillis ();

  /**
   * Gets all the output directories to register with the project for
   * compilation.
   *
   * @return The compile source roots to register with the project, never
   *         <code>null</code>.
   */
  protected abstract File [] getCompileSourceRoots ();

  /**
   * Gets the package into which the generated parser files should be stored.
   *
   * @return The package into which the generated parser files should be stored,
   *         can be <code>null</code> to use the package declaration from the
   *         grammar file.
   */
  // TODO: Once the parameter "packageName" from the javacc mojo has been
  // deleted, remove this method, too.
  protected String getParserPackage ()
  {
    return null;
  }

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
        determineNonGeneratedSourceRoots ();

        if (StringUtils.isEmpty (grammarEncoding))
        {
          getLog ().warn ("File encoding for grammars has not been configured, using platform default encoding, i.e. build is platform dependent!");
        }
        if (StringUtils.isEmpty (outputEncoding))
        {
          getLog ().warn ("File encoding for output has not been configured, defaulting to UTF-8!");
        }

        for (final GrammarInfo grammarInfo : grammarInfos)
        {
          processGrammar (grammarInfo);
        }

        getLog ().info ("Processed " + grammarInfos.length + " grammar" + (grammarInfos.length != 1 ? "s" : ""));
      }

    final Collection <File> compileSourceRoots = new LinkedHashSet <> (Arrays.asList (getCompileSourceRoots ()));
    for (final File file : compileSourceRoots)
    {
      addSourceRoot (file);
    }
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
      final GrammarDirectoryScanner scanner = new GrammarDirectoryScanner ();
      scanner.setSourceDirectory (getSourceDirectory ());
      scanner.setIncludes (getIncludes ());
      scanner.setExcludes (getExcludes ());
      scanner.setOutputDirectory (getOutputDirectory ());
      scanner.setParserPackage (getParserPackage ());
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
   * Gets a temporary directory within the project's build directory.
   *
   * @return The path to the temporary directory, never <code>null</code>.
   */
  protected File getTempDirectory ()
  {
    return new File (this.project.getBuild ().getDirectory (), "javacc-" + System.currentTimeMillis ());
  }

  /**
   * Deletes the specified temporary directory.
   *
   * @param tempDirectory
   *        The directory to delete, must not be <code>null</code>.
   */
  protected void deleteTempDirectory (final File tempDirectory)
  {
    try
    {
      FileUtils.deleteDirectory (tempDirectory);
    }
    catch (final IOException e)
    {
      getLog ().warn ("Failed to delete temporary directory: " + tempDirectory, e);
    }
  }

  /**
   * Scans the filesystem for output files and copies them to the specified
   * compile source root. An output file is only copied to the compile source
   * root if it doesn't already exist in another compile source root. This
   * prevents duplicate class errors during compilation in case the user
   * provided customized files in <code>src/main/java</code> or similar.
   *
   * @param packageName
   *        The name of the destination package for the output files, must not
   *        be <code>null</code>.
   * @param sourceRoot
   *        The (absolute) path to the compile source root into which the output
   *        files should eventually be copied, must not be <code>null</code>.
   * @param tempDirectory
   *        The (absolute) path to the directory to scan for generated output
   *        files, must not be <code>null</code>.
   * @param updatePattern
   *        A glob pattern that matches the (simple) names of those files which
   *        should always be updated in case we are outputting directly into
   *        <code>src/main/java</code>, may be <code>null</code>. A leading "!"
   *        may be used to negate the pattern.
   * @throws MojoExecutionException
   *         If the output files could not be copied.
   */
  protected void copyGrammarOutput (final File sourceRoot,
                                    final String packageName,
                                    final File tempDirectory,
                                    final String updatePattern) throws MojoExecutionException
  {
    try
    {
      final Collection tempFiles = FileUtils.getFiles (tempDirectory, "*.java", null);
      for (final Iterator it = tempFiles.iterator (); it.hasNext ();)
      {
        final File tempFile = (File) it.next ();

        String outputPath = "";
        if (packageName.length () > 0)
        {
          outputPath = packageName.replace ('.', '/') + '/';
        }
        outputPath += tempFile.getName ();
        final File outputFile = new File (sourceRoot, outputPath);

        final File sourceFile = findSourceFile (outputPath);

        boolean alwaysUpdate = false;
        if (updatePattern != null && sourceFile != null)
        {
          if (updatePattern.startsWith ("!"))
          {
            alwaysUpdate = !SelectorUtils.match (updatePattern.substring (1), tempFile.getName ());
          }
          else
          {
            alwaysUpdate = SelectorUtils.match (updatePattern, tempFile.getName ());
          }
        }

        if (sourceFile == null || (alwaysUpdate && sourceFile.equals (outputFile)))
        {
          getLog ().debug ("Copying generated file: " + outputPath);
          try
          {
            FileUtils.copyFile (tempFile, outputFile);
          }
          catch (final IOException e)
          {
            throw new MojoExecutionException ("Failed to copy generated source file to output directory:" +
                                              tempFile +
                                              " -> " +
                                              outputFile,
                                              e);
          }
        }
        else
        {
          getLog ().debug ("Skipping customized file: " + outputPath);
        }
      }
    }
    catch (final IOException e)
    {
      throw new MojoExecutionException ("Failed to copy generated source files", e);
    }
  }

  /**
   * Determines those compile source roots of the project that do not reside
   * below the project's build directories. These compile source roots are
   * assumed to contain hand-crafted sources that must not be overwritten with
   * generated files. In most cases, this is simply
   * "${project.build.sourceDirectory}".
   *
   * @throws MojoExecutionException
   *         If the compile source rotos could not be determined.
   */
  private void determineNonGeneratedSourceRoots () throws MojoExecutionException
  {
    this.nonGeneratedSourceRoots = new LinkedHashSet <> ();
    try
    {
      final String targetPrefix = new File (this.project.getBuild ().getDirectory ()).getCanonicalPath () +
                                  File.separator;
      final Collection sourceRoots = this.project.getCompileSourceRoots ();
      for (final Iterator it = sourceRoots.iterator (); it.hasNext ();)
      {
        File sourceRoot = new File (it.next ().toString ());
        if (!sourceRoot.isAbsolute ())
        {
          sourceRoot = new File (this.project.getBasedir (), sourceRoot.getPath ());
        }
        final String sourcePath = sourceRoot.getCanonicalPath ();
        if (!sourcePath.startsWith (targetPrefix))
        {
          this.nonGeneratedSourceRoots.add (sourceRoot);
          getLog ().debug ("Non-generated compile source root: " + sourceRoot);
        }
        else
        {
          getLog ().debug ("Generated compile source root: " + sourceRoot);
        }
      }
    }
    catch (final IOException e)
    {
      throw new MojoExecutionException ("Failed to determine non-generated source roots", e);
    }
  }

  /**
   * Determines whether the specified source file is already present in any of
   * the compile source roots registered with the current Maven project.
   *
   * @param filename
   *        The source filename to check, relative to a source root, must not be
   *        <code>null</code>.
   * @return The (absolute) path to the existing source file if any,
   *         <code>null</code> otherwise.
   */
  private File findSourceFile (final String filename)
  {
    final Collection <File> sourceRoots = this.nonGeneratedSourceRoots;
    for (final File sourceRoot : sourceRoots)
    {
      final File sourceFile = new File (sourceRoot, filename);
      if (sourceFile.exists ())
      {
        return sourceFile;
      }
    }
    return null;
  }

  /**
   * Determines whether the specified directory denotes a compile source root of
   * the current project.
   *
   * @param directory
   *        The directory to check, must not be <code>null</code>.
   * @return <code>true</code> if the specified directory is a compile source
   *         root of the project, <code>false</code> otherwise.
   */
  protected boolean isSourceRoot (final File directory)
  {
    return this.nonGeneratedSourceRoots.contains (directory);
  }

  /**
   * Registers the specified directory as a compile source root for the current
   * project.
   *
   * @param directory
   *        The absolute path to the source root, must not be <code>null</code>.
   */
  private void addSourceRoot (final File directory)
  {
    if (this.project != null)
    {
      getLog ().debug ("Adding compile source root: " + directory);
      this.project.addCompileSourceRoot (directory.getAbsolutePath ());
    }
  }

  /**
   * Creates a new facade to invoke JavaCC. Most options for the invocation are
   * derived from the current values of the corresponding mojo parameters. The
   * caller is responsible to set the input file and output directory on the
   * returned facade.
   *
   * @return The facade for the tool invocation, never <code>null</code>.
   */
  protected JavaCC newJavaCC ()
  {
    final JavaCC javacc = new JavaCC ();
    javacc.setLog (getLog ());
    javacc.setGrammarEncoding (this.grammarEncoding);
    javacc.setOutputEncoding (this.outputEncoding);
    javacc.setJdkVersion (this.jdkVersion);
    javacc.setBuildParser (this.buildParser);
    javacc.setBuildTokenManager (this.buildTokenManager);
    javacc.setCacheTokens (this.cacheTokens);
    javacc.setChoiceAmbiguityCheck (this.choiceAmbiguityCheck);
    javacc.setCommonTokenAction (this.commonTokenAction);
    javacc.setDebugLookAhead (this.debugLookAhead);
    javacc.setDebugParser (this.debugParser);
    javacc.setDebugTokenManager (this.debugTokenManager);
    javacc.setErrorReporting (this.errorReporting);
    javacc.setForceLaCheck (this.forceLaCheck);
    javacc.setIgnoreCase (this.ignoreCase);
    javacc.setJavaUnicodeEscape (this.javaUnicodeEscape);
    javacc.setKeepLineColumn (this.keepLineColumn);
    javacc.setLookAhead (this.lookAhead);
    javacc.setOtherAmbiguityCheck (this.otherAmbiguityCheck);
    javacc.setSanityCheck (this.sanityCheck);
    javacc.setTokenManagerUsesParser (this.tokenManagerUsesParser);
    javacc.setTokenExtends (this.tokenExtends);
    javacc.setTokenFactory (this.tokenFactory);
    javacc.setUnicodeInput (this.unicodeInput);
    javacc.setUserCharStream (this.userCharStream);
    javacc.setUserTokenManager (this.userTokenManager);
    javacc.setSupportClassVisibilityPublic (this.supportClassVisibilityPublic);
    javacc.setJavaTemplateType (this.javaTemplateType);
    return javacc;
  }
}
