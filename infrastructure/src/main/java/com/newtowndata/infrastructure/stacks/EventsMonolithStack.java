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
package com.newtowndata.infrastructure.stacks;

import com.newtowndata.infrastructure.utils.StackUtils;
import java.util.Map;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableEncryption;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Tracing;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSourceProps;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketAccessControl;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.sqs.QueueEncryption;
import software.constructs.Construct;

public class EventsMonolithStack extends Stack {

  public EventsMonolithStack(final Construct scope, final EventsMonolithStackProps stackProps) {
    super(scope, stackProps.stackPrefix() + "-events-monolith-demo", stackProps.props());

    Queue deadLetterQueue = Queue.Builder.create(this, "DeadLetterQueue")
        .queueName(stackProps.stackPrefix() + "-events-monolith-dlq").enforceSsl(true)
        .encryption(QueueEncryption.SQS_MANAGED).removalPolicy(RemovalPolicy.DESTROY).build();

    Queue appQueue = Queue.Builder.create(this, "AppQueue")
        .queueName(stackProps.stackPrefix() + "-events-monolith-app-queue").enforceSsl(true)
        .encryption(QueueEncryption.SQS_MANAGED)
        .deadLetterQueue(
            DeadLetterQueue.builder().queue(deadLetterQueue).maxReceiveCount(3).build())
        .removalPolicy(RemovalPolicy.DESTROY).build();

    Bucket appBucket = Bucket.Builder.create(this, "AppBucket")
        .bucketName(stackProps.stackPrefix() + "-events-monolith-store")
        .accessControl(BucketAccessControl.PRIVATE).autoDeleteObjects(true)
        .encryption(BucketEncryption.S3_MANAGED).enforceSsl(true).publicReadAccess(false)
        .removalPolicy(RemovalPolicy.DESTROY).build();

    Table appTable = Table.Builder.create(this, "AppTable").encryption(TableEncryption.DEFAULT)
        .billingMode(BillingMode.PAY_PER_REQUEST)
        .tableName(stackProps.stackPrefix() + "-events-monolith-state")
        .partitionKey(Attribute.builder().name("app_key").type(AttributeType.STRING).build())
        .sortKey(Attribute.builder().name("app_state").type(AttributeType.STRING).build())
        .removalPolicy(RemovalPolicy.DESTROY).build();

    Function app = Function.Builder.create(this, "App")
        .functionName(stackProps.stackPrefix() + "-events-monolith-app").tracing(Tracing.ACTIVE)
        .architecture(Architecture.ARM_64)
        .code(Code.fromAsset(StackUtils.getAssetPath("events-monolith-1.0.0.zip").toString()))
        .handler("com.newtowndata.events.lambda.EventsHandler::handleRequest")
        .runtime(Runtime.JAVA_17)
        .environment(Map.of("QUEUE_URL", appQueue.getQueueUrl(), "TABLE_NAME",
            appTable.getTableName(), "BUCKET_NAME", appBucket.getBucketName()))
        .memorySize(1770).build();

    appQueue.grantSendMessages(app);
    appBucket.grantReadWrite(app);
    appTable.grantReadWriteData(app);

    app.addEventSource(new SqsEventSource(appQueue,
        SqsEventSourceProps.builder().batchSize(1).maxConcurrency(2).enabled(true).build()));
  }

}
