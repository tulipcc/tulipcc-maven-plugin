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

import junit.framework.TestCase;

/**
 * Tests <code>JJDoc</code> facade.
 * 
 * @author Benjamin Bentmann
 * @version $Id: JJDocTest.java 6198 2008-02-03 21:32:31Z bentmann $
 */
public class JJDocTest extends TestCase
{

  public void testToStringNullSafe () throws Exception
  {
    final JJDoc tool = new JJDoc ();
    final String string = tool.toString ();
    assertNotNull (string);
    assertTrue (string.indexOf ("null") < 0);
  }

  public void testSettersNullSafe () throws Exception
  {
    final JJDoc tool = new JJDoc ();
    tool.setInputFile (null);
    tool.setOutputFile (null);
    tool.setOneTable (null);
    tool.setText (null);
    tool.setLog (null);
  }

}
