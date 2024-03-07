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
package com.newtowndata.events.lambda.io.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.newtowndata.events.core.logging.Logger;
import com.newtowndata.events.core.logging.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class TemporaryFileTest {

  private static final Logger LOG = LoggerFactory.create(TemporaryFileTest.class);

  @Test
  void testLifecycle() throws IOException {
    Path tempFilePath = null;
    try (TemporaryFile tempFile = new TemporaryFile("prefix", ".test")) {
      tempFilePath = tempFile.getTempFile();
      LOG.info("Temporary file: " + tempFilePath);
      assertTrue(tempFilePath.toString().matches("^.*prefix.*\\.test$"));
      assertTrue(Files.exists(tempFilePath));

      assertEquals("", Files.readString(tempFilePath, StandardCharsets.UTF_8));
      tempFile.overwrite(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)));
      assertEquals("abc", Files.readString(tempFilePath, StandardCharsets.UTF_8));
    }
    assertFalse(Files.exists(tempFilePath));
  }

  @Test
  void testOverwriteFailed() throws IOException {
    try (TemporaryFile tempFile = new TemporaryFile("prefix", ".test")) {
      assertThrows(IllegalStateException.class, () -> tempFile.overwrite(new InputStream() {

        @Override
        public int read() throws IOException {
          throw new IOException("test");
        }

      }));
    }
  }
}
