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
package com.newtowndata.events.local.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.newtowndata.events.core.io.StateTable;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LocalStateTableTest {

  private static final String TEST_STATE = "testState";
  private static final String STATE_A = "a";
  private static final String STATE_B = "b";

  @Test
  void lifecycleTest() {
    StateTable table = new LocalStateTable();

    table.deleteState(TEST_STATE);
    assertEquals(Set.of(), table.getStates(TEST_STATE));

    table.putState(TEST_STATE, STATE_A);
    table.putState(TEST_STATE, STATE_B);

    assertEquals(Set.of(STATE_A, STATE_B), table.getStates(TEST_STATE));

    table.deleteState(TEST_STATE);
    assertEquals(Set.of(), table.getStates(TEST_STATE));
  }

}
