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

import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.ApplicationState;
import com.newtowndata.events.core.OutputEvent;
import com.newtowndata.events.core.StateContext;
import com.newtowndata.events.core.logging.Logger;
import com.newtowndata.events.core.logging.LoggerFactory;
import com.newtowndata.events.core.utils.EventUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Abstract state that simplifies event publishing.
 */
public abstract class GenericState implements ApplicationState {

  private static final Logger LOG = LoggerFactory.create(GenericState.class);

  private final String stateName;

  protected GenericState(String stateName) {
    this.stateName = Objects.requireNonNull(stateName, "stateName");
  }

  @Override
  public String stateName() {
    return stateName;
  }

  @Override
  public List<OutputEvent> handleEvent(StateContext stateContext, ApplicationEvent input) {
    List<OutputEvent> outputEvents = new ArrayList<>();
    handleEvent(stateContext, input, outputEvents::add);
    LOG.info("Transition: " + EventUtils.eventToString(input) + " => "
        + outputEvents.stream().map(EventUtils::eventToString).toList());
    return outputEvents;
  }

  protected abstract void handleEvent(StateContext stateContext, ApplicationEvent input,
      Consumer<OutputEvent> outputConsumer);

}
