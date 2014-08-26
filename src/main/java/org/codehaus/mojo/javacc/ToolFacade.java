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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

/**
 * Provides a facade for the mojos to invoke JavaCC related tools.
 * 
 * @author Benjamin Bentmann
 * @version $Id: ToolFacade.java 7758 2008-09-29 20:06:33Z bentmann $
 */
abstract class ToolFacade
{

  /**
   * The logger used to output diagnostic messages.
   */
  private Log log;

  /**
   * Sets the logger used to output diagnostic messages.
   * 
   * @param logger
   *        The logger used to output diagnostic messages, may be
   *        <code>null</code>.
   */
  public void setLog (final Log logger)
  {
    this.log = logger;
  }

  /**
   * Gets the logger used to output diagnostic messages.
   * 
   * @return The logger used to output diagnostic messages, never
   *         <code>null</code>.
   */
  protected Log getLog ()
  {
    if (this.log == null)
    {
      this.log = new SystemStreamLog ();
    }
    return this.log;
  }

  /**
   * Gets the name of the tool.
   * 
   * @return The name of the tool, never <code>null</code>.
   */
  protected String getToolName ()
  {
    final String name = getClass ().getName ();
    return name.substring (name.lastIndexOf ('.') + 1);
  }

  /**
   * Runs the tool using the previously set parameters.
   * 
   * @throws MojoExecutionException
   *         If the tool could not be invoked.
   * @throws MojoFailureException
   *         If the tool reported a non-zero exit code.
   */
  public void run () throws MojoExecutionException, MojoFailureException
  {
    int exitCode;
    try
    {
      if (getLog ().isDebugEnabled ())
      {
        getLog ().debug ("Running " + getToolName () + ": " + this);
      }
      exitCode = execute ();
    }
    catch (final Exception e)
    {
      throw new MojoExecutionException ("Failed to execute " + getToolName (), e);
    }
    if (exitCode != 0)
    {
      throw new MojoFailureException (getToolName () + " reported exit code " + exitCode + ": " + this);
    }
  }

  /**
   * Runs the tool using the previously set parameters.
   * 
   * @return The exit code of the tool, non-zero means failure.
   * @throws Exception
   *         If the tool could not be invoked.
   */
  protected abstract int execute () throws Exception;

}
