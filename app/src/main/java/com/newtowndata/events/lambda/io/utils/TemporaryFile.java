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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Temporary file resource.
 */
public class TemporaryFile implements AutoCloseable {

  private final Path tempFile;

  public TemporaryFile(String prefix, String suffix) {
    try {
      tempFile = Files.createTempFile(prefix, suffix);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot create temporary file", e);
    }
  }

  public void overwrite(InputStream inputStream) {
    try (OutputStream osr = Files.newOutputStream(tempFile, StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
      inputStream.transferTo(osr);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to overwrite temp file " + tempFile, e);
    }
  }

  public Path getTempFile() {
    return tempFile;
  }

  @Override
  public void close() {
    if (tempFile != null) {
      try {
        Files.deleteIfExists(tempFile);
      } catch (IOException e) {
        throw new IllegalStateException("Failed to delete temporary file " + tempFile, e);
      }
    }
  }
}
