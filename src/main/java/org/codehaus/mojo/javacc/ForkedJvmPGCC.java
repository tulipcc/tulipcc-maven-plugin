package org.codehaus.mojo.javacc;

import com.helger.commons.id.IHasID;
import com.helger.security.messagedigest.EMessageDigestAlgorithm;

/**
 * Special implementation of {@link ForkedJvm} that adds some default class path
 * entries.
 */
class ForkedJvmPGCC extends ForkedJvm
{
  public ForkedJvmPGCC ()
  {
    addClassPathEntry (IHasID.class);
    addClassPathEntry (EMessageDigestAlgorithm.class);
  }
}
