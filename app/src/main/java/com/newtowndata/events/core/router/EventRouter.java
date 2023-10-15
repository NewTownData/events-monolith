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
package com.newtowndata.events.core.router;

import static com.newtowndata.events.core.utils.EventUtils.eventToString;
import com.newtowndata.events.core.ApplicationContext;
import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.ApplicationException;
import com.newtowndata.events.core.ApplicationState;
import com.newtowndata.events.core.OutputEvent;
import com.newtowndata.events.core.logging.Logger;
import com.newtowndata.events.core.logging.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Event router routes events to the required state and publishes the produced events.
 */
public final class EventRouter {

  private static final Logger LOG = LoggerFactory.create(EventRouter.class);

  private final ApplicationContext applicationContext;
  private final Map<String, ApplicationState> states;

  private EventRouter(ApplicationContext applicationContext, Map<String, ApplicationState> states) {
    this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext");
    this.states = Objects.requireNonNull(states, "states");
  }

  public static Builder of(ApplicationContext applicationContext) {
    return new Builder(applicationContext);
  }

  public void processEvent(ApplicationEvent input) {
    LOG.info(eventToString(input) + ": processing event");

    if (input.ttl() <= 0) {
      LOG.info(eventToString(input) + ": event expired - ignoring");
      return;
    }

    ApplicationState state = states.get(input.targetState());
    if (state == null) {
      LOG.info(eventToString(input) + ": no state defined - ignoring");
      return;
    }

    List<OutputEvent> outputEvents;
    try {
      outputEvents = state.handleEvent(applicationContext.stateContext(), input);
    } catch (Exception e) {
      LOG.error(eventToString(input) + ": state failed", e);
      throw new ApplicationException(eventToString(input) + ": state failed");
    }

    if (outputEvents == null || outputEvents.isEmpty()) {
      LOG.info(eventToString(input) + ": no output events produced");
      return;
    }

    List<ApplicationEvent> convertedOutputEvents = convertEvents(input, outputEvents);
    publishEvents(convertedOutputEvents);

    LOG.info(eventToString(input) + ": successfully processed");
  }

  private List<ApplicationEvent> convertEvents(ApplicationEvent input,
      List<OutputEvent> outputEvents) {
    return outputEvents.stream().map(event -> ApplicationEvent.of(input, event)).toList();
  }

  private void publishEvents(List<ApplicationEvent> outputEvents) {
    for (ApplicationEvent event : outputEvents) {
      LOG.info(eventToString(event) + ": producing event");
      try {
        applicationContext.eventPublisher().publishEvent(event);
      } catch (Exception e) {
        LOG.error(eventToString(event) + ": failed to produce event", e);
        throw new ApplicationException(eventToString(event) + ": failed to produce event");
      }
      LOG.info(eventToString(event) + ": event produced");
    }
  }

  /**
   * Builder for {@link EventRouter}.
   */
  public final static class Builder {

    private final ApplicationContext applicationContext;
    private final Map<String, ApplicationState> states;

    public Builder(ApplicationContext applicationContext) {
      this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext");
      this.states = new HashMap<>();
    }

    public Builder withState(ApplicationState state) {
      if (states.putIfAbsent(state.stateName(), state) != null) {
        throw new IllegalStateException("State " + state.stateName() + " is already defined");
      }
      return this;
    }

    public EventRouter build() {
      return new EventRouter(applicationContext, states);
    }
  }

}
