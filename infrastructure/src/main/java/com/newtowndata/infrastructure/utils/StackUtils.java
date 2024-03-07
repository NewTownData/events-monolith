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
package com.newtowndata.infrastructure.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StackUtils {

  public static final String APPLICATION_NAME = "events-monolith";

  private static final String BUCKET_PREFIX_DELIMITER = "/";
  private static final String DOCKER_TAG_SEPARATOR = "-";

  private StackUtils() {
  }

  public static String getStackPrefix() {
    String value = System.getenv("STACK_PREFIX");
    if (value == null) {
      throw new IllegalStateException(
          "No STACK_PREFIX environment variable defined. It must be a globally unique value.");
    }
    return value;
  }

  public static String getApplicationName() {
    return getStackPrefix() + "-" + APPLICATION_NAME;
  }

  public static Path getAssetPath(String name) {
    Path assetPath = Paths.get(System.getProperty("user.dir"), "assets", name);
    if (!Files.exists(assetPath)) {
      throw new IllegalArgumentException("No asset exists at path " + assetPath);
    }
    return assetPath;
  }

  public static String getAssetBucketPrefix(String applicationName) {
    return applicationName + BUCKET_PREFIX_DELIMITER;
  }

  public static String getAssetDockerTagPrefix(String applicationName) {
    return applicationName + DOCKER_TAG_SEPARATOR;
  }

}
