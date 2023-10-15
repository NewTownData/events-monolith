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
package com.newtowndata.events.local;

import com.newtowndata.events.core.ApplicationContext;
import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.StateContext;
import com.newtowndata.events.core.io.EventPublisher;
import com.newtowndata.events.core.logging.Logger;
import com.newtowndata.events.core.logging.LoggerFactory;
import com.newtowndata.events.core.utils.EventUtils;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

/**
 * Local implementation of {@link ApplicationContext}.
 */
public class LocalApplicationContext implements ApplicationContext {

  private static final Logger LOG = LoggerFactory.create(LocalApplicationContext.class);

  private final Queue<ApplicationEvent> queue;
  private final StateContext stateContext;

  public LocalApplicationContext(StateContext stateContext, ApplicationEvent initialEvent) {
    this.stateContext = Objects.requireNonNull(stateContext, "stateContext");
    Objects.requireNonNull(initialEvent, "initialEvent");

    this.queue = new LinkedList<>();
    LOG.info("Initialized queue with event: " + EventUtils.eventToString(initialEvent));
    eventPublisher().publishEvent(initialEvent);
  }

  @Override
  public EventPublisher eventPublisher() {
    return this::publishEvent;
  }

  private void publishEvent(ApplicationEvent event, int waitInSec) {
    Objects.requireNonNull(event, "event");
    if (waitInSec < 0) {
      throw new IllegalStateException("Negative wait time: " + waitInSec);
    }

    if (waitInSec > 0) {
      LOG.info(EventUtils.eventToString(event) + ": sleeping for " + waitInSec + " sec");
      try {
        Thread.sleep(1000L * waitInSec);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    queue.add(event);
    LOG.info("Added event to queue: " + EventUtils.eventToString(event));
  }

  @Override
  public StateContext stateContext() {
    return stateContext;
  }

  public Optional<ApplicationEvent> consumeEvent() {
    Optional<ApplicationEvent> event = Optional.ofNullable(queue.poll());
    if (event.isPresent()) {
      LOG.info("Event consumed: " + EventUtils.eventToString(event.get()));
    } else {
      LOG.info("No event found");
    }
    return event;
  }

}
