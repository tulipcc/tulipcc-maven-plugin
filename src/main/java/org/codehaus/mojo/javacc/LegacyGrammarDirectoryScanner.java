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

/**
 * Scans source directories for JavaCC grammar files. This scanner supports
 * {@link JJTreeMojo} and {@link JTBMojo} which perform timestamp checking
 * against copies of the input grammars rather than against the generated parser
 * files. Hence, the directory configured by
 * code>setOutputDirectory(File)</code> is taken to be the output directory for
 * the timestamp files.
 *
 * @author Benjamin Bentmann
 * @version $Id: LegacyGrammarDirectoryScanner.java 6251 2008-02-08 13:56:51Z
 *          bentmann $
 */
class LegacyGrammarDirectoryScanner extends GrammarDirectoryScanner
{
  /**
   * {@inheritDoc}
   */
  @Override
  protected File [] getTargetFiles (final File targetDirectory, final String grammarFile, final GrammarInfo grammarInfo)
  {
    final File timestampFile = new File (targetDirectory, grammarFile);
    return new File [] { timestampFile };
  }
}
