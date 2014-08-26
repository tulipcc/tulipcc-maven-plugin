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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Parses a JJTree grammar file (<code>*.jjt</code>) and transforms it to Java
 * source files and a JavaCC grammar file. Please see the <a
 * href="https://javacc.dev.java.net/doc/JJTree.html">JJTree Reference
 * Documentation</a> for more information.
 * 
 * @goal jjtree
 * @phase generate-sources
 * @since 2.0
 * @deprecated As of version 2.4, use the <code>jjtree-javacc</code> goal
 *             instead.
 * @author jesse <jesse.mcconnell@gmail.com>
 * @version $Id: JJTreeMojo.java 7847 2008-10-05 17:52:15Z bentmann $
 */
@Deprecated
public class JJTreeMojo extends AbstractPreprocessorMojo
{

  /**
   * The Java version for which to generate source code. Default value is
   * <code>1.4</code>.
   * 
   * @parameter property=jdkVersion
   * @since 2.4
   */
  private String jdkVersion;

  /**
   * A flag whether to generate sample implementations for
   * <code>SimpleNode</code> and any other nodes used in the grammar. Default
   * value is <code>true</code>.
   * 
   * @parameter property=buildNodeFiles
   */
  private Boolean buildNodeFiles;

  /**
   * A flag whether to generate a multi mode parse tree or a single mode parse
   * tree. Default value is <code>false</code>.
   * 
   * @parameter property=multi
   */
  private Boolean multi;

  /**
   * A flag whether to make each non-decorated production void instead of an
   * indefinite node. Default value is <code>false</code>.
   * 
   * @parameter property=nodeDefaultVoid
   */
  private Boolean nodeDefaultVoid;

  /**
   * The name of a custom class that extends <code>SimpleNode</code> and will be
   * used as the super class for the generated tree node classes. By default,
   * the tree node classes will directly extend the class
   * <code>SimpleNode</code>.
   * 
   * @parameter property=nodeClass
   * @since 2.5
   */
  private String nodeClass;

  /**
   * The name of a custom factory class used to create <code>Node</code>
   * objects. This class must have a method with the signature
   * <code>public static Node jjtCreate(int id)</code>. By default, the class
   * <code>SimpleNode</code> will be used as the factory class.
   * 
   * @parameter property=nodeFactory
   */
  private String nodeFactory;

  /**
   * The package to generate the AST node classes into. This value may use a
   * leading asterisk to reference the package of the corresponding parser. For
   * example, if the parser package is <code>org.apache</code> and this
   * parameter is set to <code>*.demo</code>, the tree node classes will be
   * located in the package <code>org.apache.demo</code>. By default, the
   * package of the corresponding parser is used.
   * 
   * @parameter property=nodePackage
   */
  private String nodePackage;

  /**
   * The prefix used to construct node class names from node identifiers in
   * multi mode. Default value is <code>AST</code>.
   * 
   * @parameter property=nodePrefix
   */
  private String nodePrefix;

  /**
   * A flag whether user-defined parser methods should be called on entry and
   * exit of every node scope. Default value is <code>false</code>.
   * 
   * @parameter property=nodeScopeHook
   */
  private Boolean nodeScopeHook;

  /**
   * A flag whether the node construction routines need an additional method
   * parameter to receive the parser object. Default value is <code>false</code>
   * .
   * 
   * @parameter property=nodeUsesParser
   */
  private Boolean nodeUsesParser;

  /**
   * A flag whether to generate code for a static parser. Note that this setting
   * must match the corresponding option for the <code>javacc</code> mojo.
   * Default value is <code>true</code>.
   * 
   * @parameter property=isStatic alias="staticOption"
   */
  private Boolean isStatic;

  /**
   * A flag whether to insert the methods <code>jjtGetFirstToken()</code>,
   * <code>jjtSetFirstToken()</code>, <code>getLastToken()</code> and
   * <code>jjtSetLastToken()</code> into the class <code>SimpleNode</code>.
   * Default value is <code>false</code>.
   * 
   * @parameter property=trackTokens
   * @since 2.5
   */
  private Boolean trackTokens;

  /**
   * A flag whether to insert a <code>jjtAccept()</code> method in the node
   * classes and to generate a visitor implementation with an entry for every
   * node type used in the grammar. Default value is <code>false</code>.
   * 
   * @parameter property=visitor
   */
  private Boolean visitor;

  /**
   * The name of a class to use for the data argument of the
   * <code>jjtAccept()</code> and <code>visit()</code> methods. Default value is
   * <code>java.lang.Object</code>.
   * 
   * @parameter property=visitorDataType
   * @since 2.5
   */
  private String visitorDataType;

  /**
   * The name of a class to use as the return type of the
   * <code>jjtAccept()</code> and <code>visit()</code> methods. Default value is
   * <code>java.lang.Object</code>.
   * 
   * @parameter property=visitorReturnType
   * @since 2.5
   */
  private String visitorReturnType;

  /**
   * The name of an exception class to include in the signature of the generated
   * <code>jjtAccept()</code> and <code>visit()</code> methods. By default, the
   * <code>throws</code> clause of the generated methods is empty such that only
   * unchecked exceptions can be thrown.
   * 
   * @parameter property=visitorException
   */
  private String visitorException;

  /**
   * Directory where the input JJTree files (<code>*.jjt</code>) are located.
   * 
   * @parameter property=sourceDirectory
   *            default-value="${basedir}/src/main/jjtree"
   */
  private File sourceDirectory;

  /**
   * Directory where the output Java files for the node classes and the JavaCC
   * grammar file will be located.
   * 
   * @parameter property=outputDirectory
   *            default-value="${project.build.directory}/generated-sources/jjtree"
   */
  private File outputDirectory;

  /**
   * The directory to store the processed input files for later detection of
   * stale sources.
   * 
   * @parameter property=timestampDirectory default-value=
   *            "${project.build.directory}/generated-sources/jjtree-timestamp"
   */
  private File timestampDirectory;

  /**
   * The granularity in milliseconds of the last modification date for testing
   * whether a source needs recompilation.
   * 
   * @parameter property=lastModGranularityMs default-value="0"
   */
  private int staleMillis;

  /**
   * A set of Ant-like inclusion patterns used to select files from the source
   * directory for processing. By default, the patterns
   * <code>**&#47;*.jjt</code> and <code>**&#47;*.JJT</code> are used to select
   * grammar files.
   * 
   * @parameter
   */
  private String [] includes;

  /**
   * A set of Ant-like exclusion patterns used to prevent certain files from
   * being processed. By default, this set is empty such that no files are
   * excluded.
   * 
   * @parameter
   */
  private String [] excludes;

  /**
   * {@inheritDoc}
   */
  @Override
  protected File getSourceDirectory ()
  {
    return this.sourceDirectory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String [] getIncludes ()
  {
    if (this.includes != null)
    {
      return this.includes;
    }
    else
    {
      return new String [] { "**/*.jjt", "**/*.JJT" };
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String [] getExcludes ()
  {
    return this.excludes;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected File getOutputDirectory ()
  {
    return this.outputDirectory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected File getTimestampDirectory ()
  {
    return this.timestampDirectory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int getStaleMillis ()
  {
    return this.staleMillis;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void processGrammar (final GrammarInfo grammarInfo) throws MojoExecutionException, MojoFailureException
  {
    final File jjtFile = grammarInfo.getGrammarFile ();

    // determine target directory for tree node files
    final String nodePackageName = grammarInfo.resolvePackageName (this.nodePackage);
    File nodeDirectory;
    if (nodePackageName != null)
    {
      nodeDirectory = new File (nodePackageName.replace ('.', File.separatorChar));
    }
    else
    {
      nodeDirectory = new File (grammarInfo.getParserDirectory ());
    }
    nodeDirectory = new File (getOutputDirectory (), nodeDirectory.getPath ());

    // generate final grammar file and node files
    final JJTree jjtree = newJJTree ();
    jjtree.setInputFile (jjtFile);
    jjtree.setOutputDirectory (nodeDirectory);
    jjtree.setNodePackage (nodePackageName);
    jjtree.run ();

    // create timestamp file
    createTimestamp (grammarInfo);
  }

  /**
   * Creates a new facade to invoke JJTree. Most options for the invocation are
   * derived from the current values of the corresponding mojo parameters. The
   * caller is responsible to set the input file, output directory and package
   * on the returned facade.
   * 
   * @return The facade for the tool invocation, never <code>null</code>.
   */
  protected JJTree newJJTree ()
  {
    final JJTree jjtree = new JJTree ();
    jjtree.setLog (getLog ());
    jjtree.setJdkVersion (this.jdkVersion);
    jjtree.setStatic (this.isStatic);
    jjtree.setBuildNodeFiles (this.buildNodeFiles);
    jjtree.setMulti (this.multi);
    jjtree.setNodeDefaultVoid (this.nodeDefaultVoid);
    jjtree.setNodeClass (this.nodeClass);
    jjtree.setNodeFactory (this.nodeFactory);
    jjtree.setNodePrefix (this.nodePrefix);
    jjtree.setNodeScopeHook (this.nodeScopeHook);
    jjtree.setNodeUsesParser (this.nodeUsesParser);
    jjtree.setTrackTokens (this.trackTokens);
    jjtree.setVisitor (this.visitor);
    jjtree.setVisitorDataType (this.visitorDataType);
    jjtree.setVisitorReturnType (this.visitorReturnType);
    jjtree.setVisitorException (this.visitorException);
    return jjtree;
  }

  /**
   * Prevents registration of our output or a following invocation of the javacc
   * mojo will cause duplicate sources which in turn will make compilation fail.
   */
  @Override
  protected void addCompileSourceRoot ()
  {
    // do nothing
  }

}
