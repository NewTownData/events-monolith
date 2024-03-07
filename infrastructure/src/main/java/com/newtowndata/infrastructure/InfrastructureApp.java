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
package com.newtowndata.infrastructure;

import com.newtowndata.infrastructure.stacks.EventsMonolithStack;
import com.newtowndata.infrastructure.stacks.EventsMonolithStackProps;
import com.newtowndata.infrastructure.utils.StackUtils;
import java.nio.file.Path;
import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.DefaultStackSynthesizer;
import software.amazon.awscdk.DefaultStackSynthesizerProps;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.cxapi.CloudAssembly;

public class InfrastructureApp {

  public static final String APPLICATION_NAME = "events-monolith";

  public static void main(final String[] args) {
    createApp(null);
  }

  public static CloudAssembly createApp(Path outputDirectory) {
    String applicationName = StackUtils.getApplicationName();

    AppProps.Builder appPropsBuilder = AppProps.builder();
    if (outputDirectory != null) {
      appPropsBuilder.outdir(outputDirectory.toString());
    }

    DefaultStackSynthesizer synthesizer = new DefaultStackSynthesizer(DefaultStackSynthesizerProps.
        builder()
        .bucketPrefix(StackUtils.getAssetBucketPrefix(applicationName))
        .dockerTagPrefix(StackUtils.getAssetDockerTagPrefix(applicationName))
        .build());
    appPropsBuilder.defaultStackSynthesizer(synthesizer);

    App app = new App(appPropsBuilder.build());
    createStacks(app);
    return app.synth();
  }

  public static void createStacks(App app) {
    String stackPrefix = StackUtils.getStackPrefix();

    new EventsMonolithStack(app,
        new EventsMonolithStackProps(stackPrefix, StackProps.builder().build())
    );
  }
}
