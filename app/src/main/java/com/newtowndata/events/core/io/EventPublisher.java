/*
 * Copyright 2023-2024 Voyta Krizek, https://github.com/NewTownData
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
package com.newtowndata.events.core.io;

import com.newtowndata.events.core.ApplicationConstants;
import com.newtowndata.events.core.ApplicationEvent;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Interface for publishing produced events.
 */
public interface EventPublisher {

  /**
   * Publish the supplied event. If the event contains attribute
   * {@link ApplicationConstants.ATTRIBUTE_WAIT_TIME_IN_SEC}, then the implementation must wait for
   * the defined number of seconds.
   * 
   * @param event Event.
   */
  default void publishEvent(ApplicationEvent event) {
    int waitInSec = computeWaitTime(event);
    ApplicationEvent updatedEvent = removeProducerAttributes(event);
    publishEvent(updatedEvent, waitInSec);
  }

  /**
   * Publish the supplied event after the wait time.
   * 
   * @param event Event.
   * @param waitInSec Implementation must wait for the defined number of seconds.
   */
  void publishEvent(ApplicationEvent event, int waitInSec);

  private ApplicationEvent removeProducerAttributes(ApplicationEvent event) {
    Map<String, String> updatedAttributes = event.attributes().entrySet().stream()
        .filter(entry -> !ApplicationConstants.ATTRIBUTE_WAIT_TIME_IN_SEC.equals(entry.getKey()))
        .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));

    return new ApplicationEvent(event.id(), event.traceId(), event.ttl(), event.sourceState(),
        event.targetState(), updatedAttributes);
  }

  private int computeWaitTime(ApplicationEvent event) {
    String waitString = event.attributes().get(ApplicationConstants.ATTRIBUTE_WAIT_TIME_IN_SEC);
    if (waitString != null) {
      int waitInSec = Integer.parseInt(waitString);
      if (waitInSec < 0) {
        throw new IllegalStateException("Negative wait time: " + waitInSec);
      }
      return waitInSec;
    }
    return 0;
  }


}
