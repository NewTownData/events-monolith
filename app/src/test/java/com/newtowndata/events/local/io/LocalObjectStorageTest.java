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
package com.newtowndata.events.local.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalObjectStorageTest {

  private static final String TEST_STORAGE = "test";
  private static final String TEST_PATH = "a/b.txt";

  @TempDir
  Path objectStorageRoot;

  private LocalObjectStorage localObjectStorage;

  @BeforeEach
  void beforeEach() {
    localObjectStorage = new LocalObjectStorage(objectStorageRoot);
  }

  @Test
  void lifecycleTest() {
    assertThrows(IllegalStateException.class,
        () -> localObjectStorage.getObject(TEST_STORAGE, TEST_PATH));
    localObjectStorage.deleteObject(TEST_STORAGE, TEST_PATH);

    String testData = "abc";
    localObjectStorage.putObject(TEST_STORAGE, TEST_PATH, testData);
    assertEquals(testData, localObjectStorage.getObjectAsString(TEST_STORAGE, TEST_PATH));

    testData = "abc123";
    localObjectStorage.putObject(TEST_STORAGE, TEST_PATH, testData);
    assertEquals(testData, localObjectStorage.getObjectAsString(TEST_STORAGE, TEST_PATH));

    localObjectStorage.deleteObject(TEST_STORAGE, TEST_PATH);

    assertThrows(IllegalStateException.class,
        () -> localObjectStorage.getObjectAsString(TEST_STORAGE, TEST_PATH));
  }

  @Test
  void deleteObjectError() throws IOException {
    localObjectStorage.putObject(TEST_STORAGE, TEST_PATH, "hello");
    assertThrows(IllegalStateException.class,
        () -> localObjectStorage.deleteObject(TEST_STORAGE, "a"));
  }

  @Test
  void putObjectError() throws IOException {
    localObjectStorage.putObject(TEST_STORAGE, TEST_PATH, "hello");
    assertThrows(IllegalStateException.class,
        () -> localObjectStorage.putObject(TEST_STORAGE, "a", "a"));
  }

  @Test
  void constructorNoRoot() throws IOException {
    assertThrows(IllegalArgumentException.class,
        () -> new LocalObjectStorage(Paths.get(objectStorageRoot.toString(), "a")));
  }

  @Test
  void constructorFileRoot() throws IOException {
    Files.writeString(Paths.get(objectStorageRoot.toString(), "a"), "test");
    assertThrows(IllegalArgumentException.class,
        () -> new LocalObjectStorage(Paths.get(objectStorageRoot.toString(), "a")));
  }

}
