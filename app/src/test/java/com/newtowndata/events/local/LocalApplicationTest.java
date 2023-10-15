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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalApplicationTest {

  @TempDir
  Path objectStorageRoot;

  @Test
  void testLoadEventFromFileNonExistent() throws IOException {
    assertThrows(IllegalStateException.class,
        () -> LocalApplication.loadEventFromFile(objectStorageRoot.toString()));
  }

  @Test
  void testMain() throws IOException {
    Path exampleFile = Paths.get(objectStorageRoot.toString(), "example.json");
    try (InputStream isr = LocalApplicationTest.class.getResourceAsStream("test-event.json")) {
      Files.write(exampleFile, isr.readAllBytes());
    }

    Path tempDir = Paths.get(objectStorageRoot.toString(), "temp");
    Files.createDirectory(tempDir);

    System.setProperty("user.dir", objectStorageRoot.toString());

    LocalApplication.main(new String[] {exampleFile.toString()});

    Path resultPath = Paths.get(tempDir.toString(), "example", "hi_all.txt");
    assertTrue(Files.exists(resultPath), resultPath.toString());

    assertEquals("Hello John\n" + //
        "Hello Alice\n" + //
        "Hello Amy", Files.readString(resultPath, StandardCharsets.UTF_8));
  }

  @Test
  void testMainInvalidArguments() throws IOException {
    assertThrows(IllegalArgumentException.class, () -> LocalApplication.main(new String[0]));
  }

}
