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
package com.newtowndata.events.lambda.io;

import com.newtowndata.events.core.io.StateTable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * AWS-specific DynamoDB implementation of {@link StateTable}.
 */
public class AwsStateTable implements StateTable {

  public static final String KEY_NAME = "app_key";
  public static final String SORT_KEY_NAME = "app_state";

  private final DynamoDbClient client;
  private final String tableName;

  AwsStateTable(String tableName, DynamoDbClient client) {
    this.tableName = Objects.requireNonNull(tableName, "tableName");
    this.client = Objects.requireNonNull(client, "client");
  }

  public AwsStateTable(String tableName) {
    this(tableName, DynamoDbClient.create());
  }

  @Override
  public Set<String> getStates(String key) {
    QueryResponse result = client.query(QueryRequest.builder().tableName(tableName)
        .keyConditionExpression(KEY_NAME + " = :" + KEY_NAME)
        .expressionAttributeValues(Map.of(":" + KEY_NAME, AttributeValue.fromS(key))).build());

    if (!result.hasItems()) {
      return Set.of();
    }

    return result.items().stream().map(row -> row.get(SORT_KEY_NAME).s())
        .collect(Collectors.toSet());
  }

  @Override
  public void putState(String key, String state) {
    client.putItem(PutItemRequest.builder().tableName(tableName)
        .item(
            Map.of(KEY_NAME, AttributeValue.fromS(key), SORT_KEY_NAME, AttributeValue.fromS(state)))
        .build());
  }

  @Override
  public void deleteState(String key) {
    Set<String> states = getStates(key);
    for (String state : states) {
      client.deleteItem(DeleteItemRequest.builder().tableName(tableName).key(
          Map.of(KEY_NAME, AttributeValue.fromS(key), SORT_KEY_NAME, AttributeValue.fromS(state)))
          .build());
    }
  }

}
