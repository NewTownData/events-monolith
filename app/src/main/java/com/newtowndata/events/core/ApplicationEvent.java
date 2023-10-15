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
package com.newtowndata.events.core;

import java.util.Map;
import java.util.Objects;

/**
 * Application event.
 */
public record ApplicationEvent(String id, String traceId, int ttl, String sourceState,
    String targetState, Map<String, String> attributes) {

  public ApplicationEvent {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(traceId, "traceId");

    if (ttl < 0) {
      throw new IllegalArgumentException("ttl must be a positive number: " + ttl);
    }

    Objects.requireNonNull(sourceState, "sourceState");
    Objects.requireNonNull(targetState, "targetState");
    Objects.requireNonNull(attributes, "attributes");
  }

  public static ApplicationEvent of(ApplicationEvent input, OutputEvent output) {
    return new ApplicationEvent(output.id(), input.traceId(), input.ttl() - 1, input.targetState(),
        output.targetState(), output.attributes());
  }

  public static ApplicationEvent ofStart(int ttl, String targetState,
      Map<String, String> attributes) {
    OutputEvent event = OutputEvent.of(targetState, attributes);
    return new ApplicationEvent(event.id(), event.id(), ttl, ApplicationConstants.STATE_START,
        event.targetState(), event.attributes());
  }

}
