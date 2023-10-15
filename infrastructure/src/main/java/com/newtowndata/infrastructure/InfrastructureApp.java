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
package com.newtowndata.infrastructure;

import com.newtowndata.infrastructure.stacks.EventsMonolithStack;
import com.newtowndata.infrastructure.stacks.EventsMonolithStackProps;
import com.newtowndata.infrastructure.utils.StackUtils;
import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

public class InfrastructureApp {

  public static void main(final String[] args) {
    String stackPrefix = StackUtils.getStackPrefix();

    App app = new App();

    new EventsMonolithStack(app, stackPrefix + "-events-monolith-demo",
        StackProps.builder().build(), new EventsMonolithStackProps(stackPrefix));

    app.synth();
  }
}

