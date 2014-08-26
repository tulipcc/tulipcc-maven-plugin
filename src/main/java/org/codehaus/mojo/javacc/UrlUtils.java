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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Assists in handling of URLs.
 * 
 * @author Benjamin Bentmann
 * @version $Id: UrlUtils.java 7084 2008-05-30 08:01:52Z bentmann $
 */
class UrlUtils
{

  /**
   * The UTF-8 character set, used to decode octets in URLs.
   */
  private static final Charset UTF8 = Charset.forName ("UTF-8");

  /**
   * The protocol prefix for "jar:" URLs.
   */
  private static final String JAR = "jar:";

  /**
   * The protocol prefix for "file:" URLs.
   */
  private static final String FILE = "file:";

  /**
   * The protocol prefix for "jar:file:" URLs.
   */
  private static final String JAR_FILE = JAR + FILE;

  /**
   * Gets the absolute filesystem path to the class path root for the specified
   * resource. The root is either a JAR file or a directory with loose class
   * files. If the URL does not use a supported protocol, an exception will be
   * thrown.
   * 
   * @param url
   *        The URL to the resource, may be <code>null</code>.
   * @param resource
   *        The name of the resource, must not be <code>null</code>.
   * @return The absolute filesystem path to the class path root of the resource
   *         or <code>null</code> if the input URL was <code>null</code>.
   */
  public static File getResourceRoot (final URL url, final String resource)
  {
    String path = null;
    if (url != null)
    {
      final String spec = url.toExternalForm ();
      if ((JAR_FILE).regionMatches (true, 0, spec, 0, JAR_FILE.length ()))
      {
        URL jar;
        try
        {
          jar = new URL (spec.substring (JAR.length (), spec.lastIndexOf ("!/")));
        }
        catch (final MalformedURLException e)
        {
          throw new IllegalArgumentException ("Invalid JAR URL: " + url + ", " + e.getMessage ());
        }
        path = decodeUrl (jar.getPath ());
      }
      else
        if (FILE.regionMatches (true, 0, spec, 0, FILE.length ()))
        {
          path = decodeUrl (url.getPath ());
          path = path.substring (0, path.length () - resource.length ());
        }
        else
        {
          throw new IllegalArgumentException ("Invalid class path URL: " + url);
        }
    }
    return (path != null) ? new File (path) : null;
  }

  /**
   * Decodes the specified URL as per RFC 3986, i.e. transforms percent-encoded
   * octets to characters by decoding with the UTF-8 character set. This
   * function is primarily intended for usage with {@link java.net.URL} which
   * unfortunately does not enforce proper URLs. As such, this method will
   * leniently accept invalid characters or malformed percent-encoded octets and
   * simply pass them literally through to the result string. Except for rare
   * edge cases, this will make unencoded URLs pass through unaltered.
   * 
   * @param url
   *        The URL to decode, may be <code>null</code>.
   * @return The decoded URL or <code>null</code> if the input was
   *         <code>null</code>.
   */
  public static String decodeUrl (final String url)
  {
    String decoded = url;
    if (url != null && url.indexOf ('%') >= 0)
    {
      final int n = url.length ();
      final StringBuffer buffer = new StringBuffer ();
      final ByteBuffer bytes = ByteBuffer.allocate (n);
      for (int i = 0; i < n;)
      {
        if (url.charAt (i) == '%')
        {
          try
          {
            do
            {
              final byte octet = (byte) Integer.parseInt (url.substring (i + 1, i + 3), 16);
              bytes.put (octet);
              i += 3;
            } while (i < n && url.charAt (i) == '%');
            continue;
          }
          catch (final RuntimeException e)
          {
            // malformed percent-encoded octet, fall through and append
            // characters literally
          }
          finally
          {
            if (bytes.position () > 0)
            {
              bytes.flip ();
              buffer.append (UTF8.decode (bytes).toString ());
              bytes.clear ();
            }
          }
        }
        buffer.append (url.charAt (i++));
      }
      decoded = buffer.toString ();
    }
    return decoded;
  }

}
