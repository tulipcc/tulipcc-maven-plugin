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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.FileUtils;

/**
 * This bean holds some output related information about a JavaCC grammar file.
 * It assists in determining the exact output location for the generated parser
 * file.
 * 
 * @author Benjamin Bentmann
 * @version $Id: GrammarInfo.java 8156 2008-11-26 18:20:19Z bentmann $
 */
class GrammarInfo
{

  /**
   * The absolute path to the base directory in which the grammar file resides.
   */
  private final File sourceDirectory;

  /**
   * The path to the grammar file (relative to its source directory, e.g.
   * "grammars/MyParser.jj").
   */
  private final String grammarFile;

  /**
   * The declared package for the generated parser (e.g. "org.apache").
   */
  private final String parserPackage;

  /**
   * The path to the directory of the parser package (relative to a source root
   * directory, e.g. "org/apache").
   */
  private final String parserDirectory;

  /**
   * The simple name of the generated parser (e.g. "MyParser").
   */
  private final String parserName;

  /**
   * The path to the generated parser file (relative to a source root directory,
   * e.g. "org/apache/MyParser.java").
   */
  private final String parserFile;

  /**
   * Creates a new info from the specified grammar file.
   * 
   * @param sourceDir
   *        The absolute path to the base directory in which the grammar file
   *        resides, must not be <code>null</code>.
   * @param inputFile
   *        The path to the grammar file (relative to the source directory),
   *        must not be <code>null</code>.
   * @throws IOException
   *         If reading the grammar file failed.
   */
  public GrammarInfo (final File sourceDir, final String inputFile) throws IOException
  {
    this (sourceDir, inputFile, null);
  }

  /**
   * Creates a new info from the specified grammar file.
   * 
   * @param sourceDir
   *        The absolute path to the base directory in which the grammar file
   *        resides, must not be <code>null</code>.
   * @param inputFile
   *        The path to the grammar file (relative to the source directory),
   *        must not be <code>null</code>.
   * @param packageName
   *        The package name for the generated parser, may be <code>null</code>
   *        to use the package declaration from the grammar file.
   * @throws IOException
   *         If reading the grammar file failed.
   */
  public GrammarInfo (final File sourceDir, final String inputFile, final String packageName) throws IOException
  {
    if (!sourceDir.isAbsolute ())
    {
      throw new IllegalArgumentException ("source directory is not absolute: " + sourceDir);
    }
    this.sourceDirectory = sourceDir;

    final File inFile = new File (inputFile);
    if (!inFile.isAbsolute ())
    {
      this.grammarFile = inFile.getPath ();
    }
    else
      if (inFile.getPath ().startsWith (sourceDir.getPath ()))
      {
        this.grammarFile = inFile.getPath ().substring (sourceDir.getPath ().length () + 1);
      }
      else
      {
        throw new IllegalArgumentException ("input file is not relative to source directory:" + inputFile);
      }

    // NOTE: JavaCC uses the platform default encoding to read files, so must we
    final String grammar = FileUtils.fileRead (getGrammarFile ());

    // TODO: Once the parameter "packageName" from the javacc mojo has been
    // deleted, remove our parameter, too.
    if (packageName == null)
    {
      this.parserPackage = findPackageName (grammar);
    }
    else
    {
      this.parserPackage = packageName;
    }

    this.parserDirectory = this.parserPackage.replace ('.', File.separatorChar);

    final String name = findParserName (grammar);
    if (name.length () <= 0)
    {
      this.parserName = FileUtils.removeExtension (inFile.getName ());
    }
    else
    {
      this.parserName = name;
    }

    if (this.parserDirectory.length () > 0)
    {
      this.parserFile = new File (this.parserDirectory, this.parserName + ".java").getPath ();
    }
    else
    {
      this.parserFile = this.parserName + ".java";
    }
  }

  /**
   * Extracts the declared package name from the specified grammar file.
   * 
   * @param grammar
   *        The contents of the grammar file, must not be <code>null</code>.
   * @return The declared package name or an empty string if not found.
   */
  private String findPackageName (final String grammar)
  {
    final String packageDeclaration = "package\\s+([^\\s.;]+(\\.[^\\s.;]+)*)\\s*;";
    final Matcher matcher = Pattern.compile (packageDeclaration).matcher (grammar);
    if (matcher.find ())
    {
      return matcher.group (1);
    }
    return "";
  }

  /**
   * Extracts the simple parser name from the specified grammar file.
   * 
   * @param grammar
   *        The contents of the grammar file, must not be <code>null</code>.
   * @return The parser name or an empty string if not found.
   */
  private String findParserName (final String grammar)
  {
    final String parserBegin = "PARSER_BEGIN\\s*\\(\\s*([^\\s\\)]+)\\s*\\)";
    final Matcher matcher = Pattern.compile (parserBegin).matcher (grammar);
    if (matcher.find ())
    {
      return matcher.group (1);
    }
    return "";
  }

  /**
   * Gets the absolute path to the base directory in which the grammar file
   * resides. Note that this is not necessarily the parent directory of the
   * grammar file.
   * 
   * @return The absolute path to the base directory in which the grammar file
   *         resides, never <code>null</code>.
   */
  public File getSourceDirectory ()
  {
    return this.sourceDirectory;
  }

  /**
   * Gets the absolute path to the grammar file.
   * 
   * @return The absolute path to the grammar file, never <code>null</code>.
   */
  public File getGrammarFile ()
  {
    return new File (this.sourceDirectory, this.grammarFile);
  }

  /**
   * Gets the path to the grammar file (relative to its source directory).
   * 
   * @return The path to the grammar file (relative to its source directory),
   *         never <code>null</code>.
   */
  public String getRelativeGrammarFile ()
  {
    return this.grammarFile;
  }

  /**
   * Resolves the specified package name against the package name of the parser
   * generated from this grammar. To reference the parser package, the input
   * string may use the prefix "*". For example, if the package for the parser
   * is "org.apache" and the input string is "*.node", the resolved package is
   * "org.apache.node". The period after the asterisk is significant, i.e. in
   * the previous example the input string "*node" would resolve to
   * "org.apachenode".
   * 
   * @param packageName
   *        The package name to resolve, may be <code>null</code>.
   * @return The resolved package name or <code>null</code> if the input string
   *         was <code>null</code>.
   */
  public String resolvePackageName (final String packageName)
  {
    String resolvedPackageName = packageName;
    if (resolvedPackageName != null && resolvedPackageName.startsWith ("*"))
    {
      resolvedPackageName = getParserPackage () + resolvedPackageName.substring (1);
      if (resolvedPackageName.startsWith ("."))
      {
        resolvedPackageName = resolvedPackageName.substring (1);
      }
    }
    return resolvedPackageName;
  }

  /**
   * Gets the declared package for the generated parser (e.g. "org.apache").
   * 
   * @return The declared package for the generated parser (e.g. "org.apache")
   *         or an empty string if no package declaration was found, never
   *         <code>null</code>.
   */
  public String getParserPackage ()
  {
    return this.parserPackage;
  }

  /**
   * Gets the path to the directory of the parser package (relative to a source
   * root directory, e.g. "org/apache").
   * 
   * @return The path to the directory of the parser package (relative to a
   *         source root directory, e.g. "org/apache") or an empty string if no
   *         package declaration was found, never <code>null</code>.
   */
  public String getParserDirectory ()
  {
    return this.parserDirectory;
  }

  /**
   * Gets the simple name of the generated parser (e.g. "MyParser")
   * 
   * @return The simple name of the generated parser (e.g. "MyParser"), never
   *         <code>null</code>.
   */
  public String getParserName ()
  {
    return this.parserName;
  }

  /**
   * Gets the path to the parser file (relative to a source root directory, e.g.
   * "org/apache/MyParser.java").
   * 
   * @return The path to the parser file (relative to a source root directory,
   *         e.g. "org/apache/MyParser.java"), never <code>null</code>.
   */
  public String getParserFile ()
  {
    return this.parserFile;
  }

  /**
   * Gets a string representation of this bean. This value is for debugging
   * purposes only.
   * 
   * @return A string representation of this bean.
   */
  @Override
  public String toString ()
  {
    return getGrammarFile () + " -> " + getParserFile ();
  }

}
