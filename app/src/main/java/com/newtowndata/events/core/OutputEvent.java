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
package com.newtowndata.events.core;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Output event from the state.
 */
public record OutputEvent(String id, String targetState, Map<String, String> attributes) {

  public OutputEvent {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(targetState, "targetState");
    Objects.requireNonNull(attributes, "attributes");
  }

  public static OutputEvent of(String targetState, Map<String, String> attributes) {
    return new OutputEvent(UUID.randomUUID().toString(), targetState, attributes);
  }

}
