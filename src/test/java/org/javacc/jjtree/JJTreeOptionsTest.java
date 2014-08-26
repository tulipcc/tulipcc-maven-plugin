package org.javacc.jjtree;

import java.io.File;

import junit.framework.TestCase;

import org.javacc.parser.JavaCCErrors;
import org.javacc.parser.Options;

/**
 * Test the JJTree-specific options.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public final class JJTreeOptionsTest extends TestCase
{
  public void testOutputDirectory ()
  {
    JJTreeOptions.init ();
    JavaCCErrors.reInit ();

    assertEquals (new File ("."), Options.getOutputDirectory ());
    assertEquals (new File ("."), JJTreeOptions.getJJTreeOutputDirectory ());

    Options.setInputFileOption (null, null, Options.USEROPTION__OUTPUT_DIRECTORY, "test/output");
    assertEquals (new File ("test/output"), Options.getOutputDirectory ());
    assertEquals (new File ("test/output"), JJTreeOptions.getJJTreeOutputDirectory ());

    Options.setInputFileOption (null, null, "JJTREE_OUTPUT_DIRECTORY", "test/jjtreeoutput");
    assertEquals (new File ("test/output"), Options.getOutputDirectory ());
    assertEquals (new File ("test/jjtreeoutput"), JJTreeOptions.getJJTreeOutputDirectory ());

    assertEquals (0, JavaCCErrors.get_warning_count ());
    assertEquals (0, JavaCCErrors.get_error_count ());
    assertEquals (0, JavaCCErrors.get_parse_error_count ());
    assertEquals (0, JavaCCErrors.get_semantic_error_count ());
  }

  public void testNodeFactory ()
  {
    JJTreeOptions.init ();
    JavaCCErrors.reInit ();

    assertEquals (0, JavaCCErrors.get_warning_count ());
    assertEquals (0, JavaCCErrors.get_error_count ());
    Options.setInputFileOption (null, null, "NODE_FACTORY", Boolean.FALSE);
    assertEquals (JJTreeOptions.getNodeFactory (), "");

    JJTreeOptions.init ();
    Options.setInputFileOption (null, null, "NODE_FACTORY", Boolean.TRUE);
    assertEquals (JJTreeOptions.getNodeFactory (), "*");

    JJTreeOptions.init ();
    Options.setInputFileOption (null, null, "NODE_FACTORY", "mypackage.MyNode");
    assertEquals (JJTreeOptions.getNodeFactory (), "mypackage.MyNode");

    assertEquals (0, JavaCCErrors.get_warning_count ());

    assertEquals (0, JavaCCErrors.get_error_count ());
    assertEquals (0, JavaCCErrors.get_parse_error_count ());
    assertEquals (0, JavaCCErrors.get_semantic_error_count ());
  }

  public void testNodeClass ()
  {
    JJTreeOptions.init ();
    JavaCCErrors.reInit ();

    assertEquals (0, JavaCCErrors.get_warning_count ());
    assertEquals (0, JavaCCErrors.get_error_count ());

    assertEquals ("", JJTreeOptions.getNodeClass ());
    // Need some functional tests, as well.
  }

  public void testValidate ()
  {
    JJTreeOptions.init ();
    JavaCCErrors.reInit ();

    Options.setCmdLineOption ("VISITOR_DATA_TYPE=Object");
    JJTreeOptions.validate ();
    assertEquals (1, JavaCCErrors.get_warning_count ());

    JJTreeOptions.init ();
    JavaCCErrors.reInit ();

    Options.setCmdLineOption ("VISITOR_DATA_TYPE=Object");
    Options.setCmdLineOption ("VISITOR=true");
    JJTreeOptions.validate ();
    assertEquals (0, JavaCCErrors.get_warning_count ());

    JJTreeOptions.init ();
    JavaCCErrors.reInit ();

    Options.setCmdLineOption ("VISITOR_DATA_TYPE=Object");
    JJTreeOptions.validate ();
    assertEquals (1, JavaCCErrors.get_warning_count ());
  }

  public void testValidateReturnType ()
  {
    JJTreeOptions.init ();
    JavaCCErrors.reInit ();

    Options.setCmdLineOption ("VISITOR_DATA_TYPE=String");
    JJTreeOptions.validate ();
    assertEquals (1, JavaCCErrors.get_warning_count ());

    JJTreeOptions.init ();
    JavaCCErrors.reInit ();

    Options.setCmdLineOption ("VISITOR_DATA_TYPE=String");
    Options.setCmdLineOption ("VISITOR=true");
    JJTreeOptions.validate ();
    assertEquals (0, JavaCCErrors.get_warning_count ());

    JJTreeOptions.init ();
    JavaCCErrors.reInit ();

    Options.setCmdLineOption ("VISITOR_DATA_TYPE=String");
    JJTreeOptions.validate ();
    assertEquals (1, JavaCCErrors.get_warning_count ());
  }
}
