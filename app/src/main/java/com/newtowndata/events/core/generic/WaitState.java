/*
 * Copyright 2023 Voyta Krizek, https://github.com/NewTownData
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.newtowndata.events.core.generic;

import com.newtowndata.events.core.ApplicationConstants;
import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.OutputEvent;
import com.newtowndata.events.core.StateContext;
import com.newtowndata.events.core.logging.Logger;
import com.newtowndata.events.core.logging.LoggerFactory;
import com.newtowndata.events.core.utils.EventUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * State that produces and event that should be delivered after the predefined time.
 */
public class WaitState extends GenericState {

  private static final Logger LOG = LoggerFactory.create(WaitState.class);

  private final String targetState;
  private final int waitDurationInSec;

  public WaitState(String stateName, String targetState, int waitDurationInSec) {
    super(stateName);
    this.targetState = Objects.requireNonNull(targetState, "targetState");
    this.waitDurationInSec = waitDurationInSec;
    if (waitDurationInSec < 0) {
      throw new IllegalArgumentException("Wait duration cannot be negative: " + waitDurationInSec);
    }
  }

  @Override
  protected void handleEvent(StateContext stateContext, ApplicationEvent input,
      Consumer<OutputEvent> outputConsumer) {
    Map<String, String> attributes = new HashMap<>(input.attributes());
    attributes.put(ApplicationConstants.ATTRIBUTE_WAIT_TIME_IN_SEC,
        Integer.toString(waitDurationInSec));

    OutputEvent event = OutputEvent.of(targetState, attributes);
    outputConsumer.accept(event);
    LOG.info(EventUtils.eventToString(event) + ": waiting for " + waitDurationInSec + " sec");
  }

}
