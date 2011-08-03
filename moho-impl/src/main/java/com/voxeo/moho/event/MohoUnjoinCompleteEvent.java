/**
 * Copyright 2010-2011 Voxeo Corporation Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.voxeo.moho.event;

import com.voxeo.moho.Participant;

public class MohoUnjoinCompleteEvent extends MohoEvent<EventSource> implements UnjoinCompleteEvent {

  protected Participant _participant;

  protected Cause _cause;

  protected Exception _exception;

  public MohoUnjoinCompleteEvent(final EventSource source, final Participant p, final Cause cause) {
    super(source);
    _participant = p;
    _cause = cause;
  }

  public MohoUnjoinCompleteEvent(final EventSource source, final Participant p, final Cause cause, final Exception e) {
    super(source);
    _participant = p;
    _cause = cause;
    _exception = e;
  }

  @Override
  public Participant getParticipant() {
    return _participant;
  }

  @Override
  public Cause getCause() {
    return _cause;
  }

  @Override
  public Exception getException() {
    return _exception;
  }
}