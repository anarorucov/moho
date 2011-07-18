/**
 * Copyright 2010-2011 Voxeo Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.voxeo.moho;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * <p>This exception is thrown when application has made a blocking outbound call (see below) 
 * and the call receives a redirect signal from the callee.</p>
 * <blockqoute><pre><code>
 *  ApplicationContext context = ...; // context is available when the application is initialized.
 *  CallableEndpoint callee = (CallableEndponint) context.createEndpoint("sip:john@acme.com");
 *  try {
 *    callee.call("sip:doe@acme.com").join().get();
 *  }
 *  catch(RedirectException e) { 
 *    // do something
 *  }
 *  </code></pre></blockquote>
 */
public class RedirectException extends SignalException {

  private static final long serialVersionUID = -8620383625996859747L;

  protected List<String> _targets = new ArrayList<String>();

  public RedirectException(final ListIterator<String> targets) {
    while (targets.hasNext()) {
      _targets.add(targets.next());
    }
  }

  public String getTarget() {
    return _targets.size() > 0 ? _targets.get(0) : null;
  }

  public List<String> getTargets() {
    return new ArrayList<String>(_targets);
  }
}
