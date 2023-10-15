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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.newtowndata.events.Application;
import com.newtowndata.events.core.ApplicationEvent;
import com.newtowndata.events.core.StateContext;
import com.newtowndata.events.core.logging.Logger;
import com.newtowndata.events.core.logging.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * Local standalone instance of {@link Application};
 */
public class LocalApplication {

  private static final Logger LOG = LoggerFactory.create(LocalApplication.class);

  private LocalApplication() {}

  public static void run(Path objectStorageRoot, ApplicationEvent initialEvent) {
    StateContext stateContext = new LocalStateContext(objectStorageRoot);
    LocalApplicationContext localContext = new LocalApplicationContext(stateContext, initialEvent);
    Application application = new Application(localContext);

    Optional<ApplicationEvent> event;
    while ((event = localContext.consumeEvent()).isPresent()) {
      LOG.info("----- START -----");
      application.getEventRouter().processEvent(event.get());
      LOG.info("----- END -----");
    }

    LOG.info("Done");
  }

  public static void main(String[] args) {
    if (args.length < 1) {
      throw new IllegalArgumentException("Missing input event file. Example: event.json");
    }

    ApplicationEvent initialEvent = loadEventFromFile(args[0]);
    run(Paths.get(System.getProperty("user.dir"), "temp"), initialEvent);
  }

  static ApplicationEvent loadEventFromFile(String file) {
    Path path = Paths.get(file);
    try (InputStream isr = Files.newInputStream(path, StandardOpenOption.READ)) {
      return loadEventFromStream(isr);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot load file " + file, e);
    }
  }

  static ApplicationEvent loadEventFromStream(InputStream isr) throws IOException {
    Gson gson = new GsonBuilder().create();
    return gson.fromJson(new String(isr.readAllBytes(), StandardCharsets.UTF_8),
        ApplicationEvent.class);
  }

}
