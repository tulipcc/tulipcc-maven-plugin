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

import junit.framework.TestCase;

/**
 * Tests <code>UrlUtils</code>.
 * 
 * @author Benjamin Bentmann
 * @version $Id: UrlUtilsTest.java 7083 2008-05-29 23:13:24Z bentmann $
 */
public class UrlUtilsTest extends TestCase
{

  public void testGetResourceRootFileWin () throws Exception
  {
    assertMatch ("/C:/a dir", "file:/C:/a%20dir/org/Foo.class", "org/Foo.class");
    assertMatch ("/C:/a dir", "file://localhost/C:/a%20dir/org/Foo.class", "org/Foo.class");
    assertMatch ("/C:/a dir", "file:///C:/a%20dir/org/Foo.class", "org/Foo.class");
    assertMatch ("/C:/a dir", "file:/C:/a%20dir/org/Foo.class", "/org/Foo.class");
    assertMatch ("/C:/a dir", "file:/C:/a dir/org/Foo.class", "org/Foo.class");
  }

  public void testGetResourceRootJarFileWin () throws Exception
  {
    assertMatch ("/C:/a dir/t-1.jar", "jar:file:/C:/a%20dir/t-1.jar!/org/Foo.class", "org/Foo.class");
    assertMatch ("/C:/a dir/t-1.jar", "jar:file://localhost/C:/a%20dir/t-1.jar!/org/Foo.class", "org/Foo.class");
    assertMatch ("/C:/a dir/t-1.jar", "jar:file:///C:/a%20dir/t-1.jar!/org/Foo.class", "org/Foo.class");
    assertMatch ("/C:/a dir/t-1.jar", "jar:file:/C:/a%20dir/t-1.jar!/org/Foo.class", "/org/Foo.class");
    assertMatch ("/C:/a dir/t-1.jar", "jar:file:/C:/a dir/t-1.jar!/org/Foo.class", "org/Foo.class");
  }

  public void testGetResourceRootFileWinUnc () throws Exception
  {
    assertMatch ("//host/a dir", "file:////host/a%20dir/org/Foo.class", "org/Foo.class");
  }

  public void testGetResourceRootJarFileWinUnc () throws Exception
  {
    assertMatch ("//host/a dir/t-1.jar", "jar:file:////host/a%20dir/t-1.jar!/org/Foo.class", "org/Foo.class");
  }

  public void testGetResourceRootFileUnix () throws Exception
  {
    assertMatch ("/home/a dir", "file:/home/a%20dir/org/Foo.class", "org/Foo.class");
    assertMatch ("/home/a dir", "file://localhost/home/a%20dir/org/Foo.class", "org/Foo.class");
    assertMatch ("/home/a dir", "file:///home/a%20dir/org/Foo.class", "org/Foo.class");
    assertMatch ("/home/a dir", "file:/home/a%20dir/org/Foo.class", "/org/Foo.class");
    assertMatch ("/home/a dir", "file:/home/a dir/org/Foo.class", "org/Foo.class");
  }

  public void testGetResourceRootJarFileUnix () throws Exception
  {
    assertMatch ("/home/a dir/t-1.jar", "jar:file:/home/a%20dir/t-1.jar!/org/Foo.class", "org/Foo.class");
    assertMatch ("/home/a dir/t-1.jar", "jar:file://localhost/home/a%20dir/t-1.jar!/org/Foo.class", "org/Foo.class");
    assertMatch ("/home/a dir/t-1.jar", "jar:file:///home/a%20dir/t-1.jar!/org/Foo.class", "org/Foo.class");
    assertMatch ("/home/a dir/t-1.jar", "jar:file:/home/a%20dir/t-1.jar!/org/Foo.class", "/org/Foo.class");
    assertMatch ("/home/a dir/t-1.jar", "jar:file:/home/a dir/t-1.jar!/org/Foo.class", "org/Foo.class");
  }

  public void testGetResourceRootNullSafe () throws Exception
  {
    assertNull (UrlUtils.getResourceRoot (null, ""));
  }

  public void testGetResourceRootUnknownProtocal () throws Exception
  {
    try
    {
      UrlUtils.getResourceRoot (new URL ("http://www.foo.bar/index.html"), "index.html");
      fail ("Missing runtime exception");
    }
    catch (final RuntimeException e)
    {
      assertTrue (true);
    }
  }

  private void assertMatch (final String expectedFile, final String url, final String resource) throws Exception
  {
    assertEquals (new File (expectedFile), UrlUtils.getResourceRoot (new URL (url), resource));
  }

  public void testDecodeUrl ()
  {
    assertEquals ("", UrlUtils.decodeUrl (""));
    assertEquals ("foo", UrlUtils.decodeUrl ("foo"));
    assertEquals ("+", UrlUtils.decodeUrl ("+"));
    assertEquals ("% ", UrlUtils.decodeUrl ("%25%20"));
    assertEquals ("%20", UrlUtils.decodeUrl ("%2520"));
    assertEquals ("jar:file:/C:/dir/sub dir/1.0/foo-1.0.jar!/org/Bar.class",
                  UrlUtils.decodeUrl ("jar:file:/C:/dir/sub%20dir/1.0/foo-1.0.jar!/org/Bar.class"));
  }

  public void testDecodeUrlLenient ()
  {
    assertEquals (" ", UrlUtils.decodeUrl (" "));
    assertEquals ("\u00E4\u00F6\u00FC\u00DF", UrlUtils.decodeUrl ("\u00E4\u00F6\u00FC\u00DF"));
    assertEquals ("%", UrlUtils.decodeUrl ("%"));
    assertEquals ("%2", UrlUtils.decodeUrl ("%2"));
    assertEquals ("%2G", UrlUtils.decodeUrl ("%2G"));
  }

  public void testDecodeUrlNullSafe ()
  {
    assertNull (UrlUtils.decodeUrl (null));
  }

  public void testDecodeUrlEncodingUtf8 ()
  {
    assertEquals ("\u00E4\u00F6\u00FC\u00DF", UrlUtils.decodeUrl ("%C3%A4%C3%B6%C3%BC%C3%9F"));
  }

}
