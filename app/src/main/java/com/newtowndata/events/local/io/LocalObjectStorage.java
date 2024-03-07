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

import com.newtowndata.events.core.io.ObjectStorage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Local filesystem backed implementation of {@ObjectStorage}.
 */
public class LocalObjectStorage implements ObjectStorage {

  private final Path objectStorageRoot;

  public LocalObjectStorage(Path objectStorageRoot) {
    this.objectStorageRoot = Objects.requireNonNull(objectStorageRoot, "objectStorageRoot");
    if (!Files.exists(objectStorageRoot) || !Files.isDirectory(objectStorageRoot)) {
      throw new IllegalArgumentException(
          "Provided object storage root is not a directory: " + objectStorageRoot);
    }
  }

  @Override
  public InputStream getObject(String storageName, String path) {
    Path computedPath = computePath(storageName, path);
    try {
      return Files.newInputStream(computedPath, StandardOpenOption.READ);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read path " + computedPath, e);
    }
  }

  @Override
  public void putObject(String storageName, String path, InputStream inputStream) {
    Path computedPath = computePath(storageName, path);
    Path parentPath = computedPath.getParent();
    if (!Files.exists(parentPath)) {
      try {
        Files.createDirectories(parentPath);
      } catch (IOException e) {
        throw new IllegalStateException("Cannot create parent directory " + parentPath, e);
      }
    }

    try (
        OutputStream osr = Files.newOutputStream(computedPath, StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
      inputStream.transferTo(osr);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot write path " + computedPath, e);
    }
  }

  @Override
  public void deleteObject(String storageName, String path) {
    Path computedPath = computePath(storageName, path);
    try {
      Files.deleteIfExists(computedPath);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot delete path " + computedPath, e);
    }
  }


  Path computePath(String storageName, String path) {
    Objects.requireNonNull(storageName, "storageName");
    Objects.requireNonNull(path, "path");

    return Paths.get(objectStorageRoot.toString(), storageName, parsePath(path).toString());
  }

  Path parsePath(String path) {
    String[] components = path.split(Pattern.quote(ObjectStorage.PATH_SEPARATOR));
    Path result = null;

    for (int i = 0; i < components.length; i++) {
      if (i == 0) {
        result = Paths.get(components[0]);
      } else {
        result = Paths.get(result.toString(), components[i]);
      }
    }

    return result;
  }

}
