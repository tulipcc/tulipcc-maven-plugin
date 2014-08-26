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

import junit.framework.TestCase;

/**
 * Tests <code>JTB</code> facade.
 * 
 * @author Benjamin Bentmann
 * @version $Id: JTBTest.java 6281 2008-02-09 21:45:03Z bentmann $
 */
public class JTBTest extends TestCase
{

  public void testToStringNullSafe () throws Exception
  {
    final JTB tool = new JTB ();
    final String string = tool.toString ();
    assertNotNull (string);
    assertTrue (string.indexOf ("null") < 0);
  }

  public void testSettersNullSafe () throws Exception
  {
    final JTB tool = new JTB ();
    tool.setInputFile (null);
    tool.setOutputDirectory (null);
    tool.setNodeDirectory (null);
    tool.setVisitorDirectory (null);
    tool.setDescriptiveFieldNames (null);
    tool.setJavadocFriendlyComments (null);
    tool.setNodePackageName (null);
    tool.setNodeParentClass (null);
    tool.setPackageName (null);
    tool.setParentPointers (null);
    tool.setPrinter (null);
    tool.setScheme (null);
    tool.setSpecialTokens (null);
    tool.setSupressErrorChecking (null);
    tool.setVisitorPackageName (null);
    tool.setLog (null);
  }

  public void testGetOutputFile () throws Exception
  {
    final File input = new File ("Test.jtb").getAbsoluteFile ();
    final File outdir = new File ("dir").getAbsoluteFile ();

    final JTB tool = new JTB ();
    tool.setInputFile (input);
    tool.setOutputDirectory (outdir);
    final File output = tool.getOutputFile ();

    assertEquals (new File (outdir, "Test.jj"), output);
  }

}
