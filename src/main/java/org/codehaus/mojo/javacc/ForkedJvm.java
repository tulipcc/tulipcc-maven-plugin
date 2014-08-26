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
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Runs the <code>main()</code> method of some tool in a forked JVM.
 * 
 * @author Benjamin Bentmann
 * @version $Id: ForkedJvm.java 7758 2008-09-29 20:06:33Z bentmann $
 * @see <a
 *      href="http://java.sun.com/javase/6/docs/technotes/tools/windows/java.html">java
 *      - The Java Application Launcher</a>
 */
class ForkedJvm
{

  /**
   * The consumer for <code>System.out</code> messages.
   */
  private StreamConsumer systemOut;

  /**
   * The consumer for <code>System.err</code> messages.
   */
  private StreamConsumer systemErr;

  /**
   * The executable used to fork the JVM.
   */
  private final String executable;

  /**
   * The working directory for the forked JVM.
   */
  private File workingDirectory;

  /**
   * The class path entries for the forked JVM, given as strings.
   */
  private final Set <String> classPathEntries = new LinkedHashSet <String> ();

  /**
   * The qualified name of the class on which to invoke the <code>main()</code>
   * method.
   */
  private String mainClass;

  /**
   * The command line arguments to pass to the <code>main()</code> method, given
   * as strings.
   */
  private final List <String> cmdLineArgs = new ArrayList <String> ();

  /**
   * Creates a new configuration to fork a JVM.
   */
  public ForkedJvm ()
  {
    this.executable = getDefaultExecutable ();
  }

  /**
   * Gets the absolute path to the JVM executable.
   * 
   * @return The absolute path to the JVM executable.
   */
  private static String getDefaultExecutable ()
  {
    return System.getProperty ("java.home") + File.separator + "bin" + File.separator + "java";
  }

  /**
   * Sets the working directory for the forked JVM.
   * 
   * @param directory
   *        The working directory for the forked JVM, may be <code>null</code>
   *        to inherit the working directory of the current JVM.
   */
  public void setWorkingDirectory (final File directory)
  {
    this.workingDirectory = directory;
  }

  /**
   * Sets the stream consumer used to handle messages from
   * <code>System.out</code>.
   * 
   * @param consumer
   *        The stream consumer, may be <code>null</code> to discard the output.
   */
  public void setSystemOut (final StreamConsumer consumer)
  {
    this.systemOut = consumer;
  }

  /**
   * Sets the stream consumer used to handle messages from
   * <code>System.err</code>.
   * 
   * @param consumer
   *        The stream consumer, may be <code>null</code> to discard the output.
   */
  public void setSystemErr (final StreamConsumer consumer)
  {
    this.systemErr = consumer;
  }

  /**
   * Gets the class path for the forked JVM.
   * 
   * @return The class path for the forked JVM.
   */
  private String getClassPath ()
  {
    return StringUtils.join (this.classPathEntries.iterator (), File.pathSeparator);
  }

  /**
   * Adds the specified path to the class path of the forked JVM.
   * 
   * @param path
   *        The path to add, may be <code>null</code>.
   */
  public void addClassPathEntry (final String path)
  {
    if (path != null)
    {
      this.classPathEntries.add (path);
    }
  }

  /**
   * Adds the specified path to the class path of the forked JVM.
   * 
   * @param path
   *        The path to add, may be <code>null</code>.
   */
  public void addClassPathEntry (final File path)
  {
    if (path != null)
    {
      this.classPathEntries.add (path.getAbsolutePath ());
    }
  }

  /**
   * Adds the source JAR of the specified class/interface to the class path of
   * the forked JVM.
   * 
   * @param type
   *        The class/interface to add, may be <code>null</code>.
   */
  public void addClassPathEntry (final Class <?> type)
  {
    addClassPathEntry (getClassSource (type));
  }

  /**
   * Gets the JAR file or directory that contains the specified class.
   * 
   * @param type
   *        The class/interface to find, may be <code>null</code>.
   * @return The absolute path to the class source location or <code>null</code>
   *         if unknown.
   */
  private static File getClassSource (final Class <?> type)
  {
    if (type != null)
    {
      final String classResource = type.getName ().replace ('.', '/') + ".class";
      return getResourceSource (classResource, type.getClassLoader ());
    }
    return null;
  }

  /**
   * Gets the JAR file or directory that contains the specified class.
   * 
   * @param className
   *        The qualified name of the class/interface to find, may be
   *        <code>null</code>.
   * @return The absolute path to the class source location or <code>null</code>
   *         if unknown.
   */
  private static File getClassSource (final String className)
  {
    if (className != null)
    {
      final String classResource = className.replace ('.', '/') + ".class";
      return getResourceSource (classResource, Thread.currentThread ().getContextClassLoader ());
    }
    return null;
  }

  /**
   * Gets the JAR file or directory that contains the specified resource.
   * 
   * @param resource
   *        The absolute name of the resource to find, may be <code>null</code>.
   * @param loader
   *        The class loader to use for searching the resource, may be
   *        <code>null</code>.
   * @return The absolute path to the resource location or <code>null</code> if
   *         unknown.
   */
  private static File getResourceSource (final String resource, final ClassLoader loader)
  {
    if (resource != null)
    {
      URL url;
      if (loader != null)
      {
        url = loader.getResource (resource);
      }
      else
      {
        url = ClassLoader.getSystemResource (resource);
      }
      return UrlUtils.getResourceRoot (url, resource);
    }
    return null;
  }

  /**
   * Sets the qualified name of the class on which to invoke the
   * <code>main()</code> method. The source of the specified class will
   * automatically be added to the class path of the forked JVM.
   * 
   * @param name
   *        The qualified name of the class on which to invoke the
   *        <code>main()</code> method.
   */
  public void setMainClass (final String name)
  {
    this.mainClass = name;
    addClassPathEntry (getClassSource (name));
  }

  /**
   * Sets the class on which to invoke the <code>main()</code> method. The
   * source of the specified class will automatically be added to the class path
   * of the forked JVM.
   * 
   * @param type
   *        The class on which to invoke the <code>main()</code> method, may be
   *        <code>null</code>.
   */
  public void setMainClass (final Class <?> type)
  {
    this.mainClass = (type != null) ? type.getName () : null;
    addClassPathEntry (type);
  }

  /**
   * Gets the command line arguments for the <code>main()</code> method.
   * 
   * @return The command line arguments for the <code>main()</code> method.
   */
  private String [] getArguments ()
  {
    return this.cmdLineArgs.toArray (new String [this.cmdLineArgs.size ()]);
  }

  /**
   * Adds the specified argument to the command line for the <code>main()</code>
   * method.
   * 
   * @param argument
   *        The argument to add, may be <code>null</code>.
   */
  public void addArgument (final String argument)
  {
    if (argument != null)
    {
      this.cmdLineArgs.add (argument);
    }
  }

  /**
   * Adds the specified file path to the command line for the
   * <code>main()</code> method.
   * 
   * @param argument
   *        The argument to add, may be <code>null</code>.
   */
  public void addArgument (final File argument)
  {
    if (argument != null)
    {
      this.cmdLineArgs.add (argument.getAbsolutePath ());
    }
  }

  /**
   * Adds the specified arguments to the command line for the
   * <code>main()</code> method.
   * 
   * @param arguments
   *        The arguments to add, may be <code>null</code>.
   */
  public void addArguments (final String [] arguments)
  {
    if (arguments != null)
    {
      for (final String argument : arguments)
      {
        addArgument (argument);
      }
    }
  }

  /**
   * Creates the command line for the new JVM based on the current
   * configuration.
   * 
   * @return The command line used to fork the JVM, never <code>null</code>.
   */
  private Commandline createCommandLine ()
  {
    /*
     * NOTE: This method is designed to work with plexus-utils:1.1 which is used
     * by all Maven versions before 2.0.6 regardless of our plugin dependency.
     * Therefore, we use setWorkingDirectory(String) rather than
     * setWorkingDirectory(File) and addArguments() rather than createArg().
     */

    final Commandline cli = new Commandline ();

    cli.setExecutable (this.executable);

    if (this.workingDirectory != null)
    {
      cli.setWorkingDirectory (this.workingDirectory.getAbsolutePath ());
    }

    final String classPath = getClassPath ();
    if (classPath != null && classPath.length () > 0)
    {
      cli.addArguments (new String [] { "-cp", classPath });
    }

    if (this.mainClass != null && this.mainClass.length () > 0)
    {
      cli.addArguments (new String [] { this.mainClass });
    }

    cli.addArguments (getArguments ());

    return cli;
  }

  /**
   * Forks a JVM using the previously set parameters.
   * 
   * @return The exit code of the forked JVM.
   * @throws Exception
   *         If the JVM could not be forked.
   */
  public int run () throws Exception
  {
    return CommandLineUtils.executeCommandLine (createCommandLine (), this.systemOut, this.systemErr);
  }

  /**
   * Gets a string representation of the command line arguments.
   * 
   * @return A string representation of the command line arguments.
   */
  @Override
  public String toString ()
  {
    return String.valueOf (createCommandLine ());
  }

}
