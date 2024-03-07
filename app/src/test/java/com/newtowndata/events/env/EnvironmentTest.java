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
package com.newtowndata.events.env;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class EnvironmentTest {

  private final Environment environment = new Environment();

  @Test
  void testGetEnvironmentVariableException() {
    assertThrows(IllegalArgumentException.class, () -> environment
        .getEnvironmentVariable("X_UNKNOWN_VARIABLE_123_" + UUID.randomUUID().toString()));
  }

  @Test
  void testGetEnvironmentVariable() {
    // let's assume PATH environment variable exists on all systems
    String value = environment.getEnvironmentVariable("PATH");
    assertNotNull(value);
  }
}
