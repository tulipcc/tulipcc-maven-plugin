/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.javacc.jjdoc;

import java.io.PrintWriter;
import java.util.Hashtable;

import org.javacc.parser.Expansion;
import org.javacc.parser.JavaCodeProduction;
import org.javacc.parser.NonTerminal;
import org.javacc.parser.NormalProduction;
import org.javacc.parser.RCharacterList;
import org.javacc.parser.RJustName;
import org.javacc.parser.RegularExpression;
import org.javacc.parser.TokenProduction;

public class BNFGenerator implements Generator
{
  private final Hashtable id_map = new Hashtable ();
  private int id = 1;
  protected PrintWriter ostr;
  private boolean printing = true;

  protected String get_id (final String nt)
  {
    String i = (String) id_map.get (nt);
    if (i == null)
    {
      i = "prod" + id++;
      id_map.put (nt, i);
    }
    return i;
  }

  protected PrintWriter create_output_stream ()
  {

    if (JJDocOptions.getOutputFile ().equals (""))
    {
      if (JJDocGlobals.input_file.equals ("standard input"))
      {
        return new java.io.PrintWriter (new java.io.OutputStreamWriter (System.out));
      }
      else
      {
        final String ext = ".bnf";
        final int i = JJDocGlobals.input_file.lastIndexOf ('.');
        if (i == -1)
        {
          JJDocGlobals.output_file = JJDocGlobals.input_file + ext;
        }
        else
        {
          final String suffix = JJDocGlobals.input_file.substring (i);
          if (suffix.equals (ext))
          {
            JJDocGlobals.output_file = JJDocGlobals.input_file + ext;
          }
          else
          {
            JJDocGlobals.output_file = JJDocGlobals.input_file.substring (0, i) + ext;
          }
        }
      }
    }
    else
    {
      JJDocGlobals.output_file = JJDocOptions.getOutputFile ();
    }
    try
    {
      ostr = new java.io.PrintWriter (new java.io.FileWriter (JJDocGlobals.output_file));
    }
    catch (final java.io.IOException e)
    {
      error ("JJDoc: can't open output stream on file " + JJDocGlobals.output_file + ".  Using standard output.");
      ostr = new java.io.PrintWriter (new java.io.OutputStreamWriter (System.out));
    }

    return ostr;
  }

  private void println (final String s)
  {
    print (s + "\n");
  }

  public void text (final String s)
  {
    if (printing && !(s.length () == 1 && (s.charAt (0) == '\n' || s.charAt (0) == '\r')))
    {
      print (s);
    }
  }

  public void print (final String s)
  {
    ostr.print (s);
  }

  public void documentStart ()
  {
    ostr = create_output_stream ();
  }

  public void documentEnd ()
  {
    ostr.close ();
  }

  public void specialTokens (final String s)
  {}

  // public void tokenStart(TokenProduction tp) {
  // printing = false;
  // }
  // public void tokenEnd(TokenProduction tp) {
  // printing = true;
  // }
  public void nonterminalsStart ()
  {}

  public void nonterminalsEnd ()
  {}

  public void tokensStart ()
  {}

  public void tokensEnd ()
  {}

  public void javacode (final JavaCodeProduction jp)
  {}

  public void expansionEnd (final Expansion e, final boolean first)
  {}

  public void nonTerminalStart (final NonTerminal nt)
  {}

  public void nonTerminalEnd (final NonTerminal nt)
  {}

  public void productionStart (final NormalProduction np)
  {
    println ("");
    print (np.getLhs () + " ::= ");
  }

  public void productionEnd (final NormalProduction np)
  {
    println ("");
  }

  public void expansionStart (final Expansion e, final boolean first)
  {
    if (!first)
    {
      print (" | ");
    }
  }

  public void reStart (final RegularExpression r)
  {
    if (r.getClass ().equals (RJustName.class) || r.getClass ().equals (RCharacterList.class))
    {
      printing = false;
    }
  }

  public void reEnd (final RegularExpression r)
  {
    printing = true;
  }

  public void debug (final String message)
  {
    System.err.println (message);
  }

  public void info (final String message)
  {
    System.err.println (message);
  }

  public void warn (final String message)
  {
    System.err.println (message);
  }

  public void error (final String message)
  {
    System.err.println (message);
  }

  public void handleTokenProduction (final TokenProduction tp)
  {
    printing = false;
    final String text = JJDoc.getStandardTokenProductionText (tp);
    text (text);
    printing = true;
  }

}
