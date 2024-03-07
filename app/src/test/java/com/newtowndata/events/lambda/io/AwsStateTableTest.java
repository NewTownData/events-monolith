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
package com.newtowndata.events.lambda.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

@ExtendWith(MockitoExtension.class)
class AwsStateTableTest {

  private static final String TEST_TABLE = "example-table";
  private static final String TEST_KEY = "key-abc";
  private static final String TEST_STATE_1 = "state-100";
  private static final String TEST_STATE_2 = "state-200";

  @Mock
  DynamoDbClient client;

  private AwsStateTable stateTable;

  @BeforeEach
  void beforeEach() {
    this.stateTable = new AwsStateTable(TEST_TABLE, client);
  }

  @Test
  void testDeleteState() {
    when(client.query(any(QueryRequest.class))).thenReturn(
      QueryResponse.builder().items(
        List.of(
          Map.of(
            "app_key", AttributeValue.fromS(TEST_KEY),
            "app_state",AttributeValue.fromS(TEST_STATE_1)
          ),
          Map.of(
            "app_key", AttributeValue.fromS(TEST_KEY),
            "app_state", AttributeValue.fromS(TEST_STATE_2)
          )
        )
      ).build());
    
    stateTable.deleteState(TEST_KEY);

    ArgumentCaptor<DeleteItemRequest> argumentCaptor = ArgumentCaptor.forClass(DeleteItemRequest.class);
    verify(client, times(2)).deleteItem(argumentCaptor.capture());

    DeleteItemRequest request1 = argumentCaptor.getAllValues().get(0);
    assertEquals(TEST_TABLE, request1.tableName());
    assertEquals(Map.of("app_key", AttributeValue.fromS(TEST_KEY), "app_state",
        AttributeValue.fromS(TEST_STATE_1)), request1.key());

    DeleteItemRequest request2 = argumentCaptor.getAllValues().get(1);
    assertEquals(TEST_TABLE, request2.tableName());
    assertEquals(Map.of("app_key", AttributeValue.fromS(TEST_KEY), "app_state",
        AttributeValue.fromS(TEST_STATE_2)), request2.key());
  }

  @Test
  void testGetStates() {
    when(client.query(any(QueryRequest.class))).thenReturn(
      QueryResponse.builder().items(
        List.of(
          Map.of(
            "app_key", AttributeValue.fromS(TEST_KEY),
            "app_state",AttributeValue.fromS(TEST_STATE_1)
          ),
          Map.of(
            "app_key", AttributeValue.fromS(TEST_KEY),
            "app_state", AttributeValue.fromS(TEST_STATE_2)
          )
        )
      ).build());

    Set<String> states = stateTable.getStates(TEST_KEY);
    assertEquals(Set.of(TEST_STATE_1, TEST_STATE_2), states);

    ArgumentCaptor<QueryRequest> argumentCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    verify(client, times(1)).query(argumentCaptor.capture());

    QueryRequest request = argumentCaptor.getValue();

    assertEquals(TEST_TABLE, request.tableName());
    assertEquals("app_key = :app_key", request.keyConditionExpression());
    assertEquals(Map.of(":app_key", AttributeValue.fromS(TEST_KEY)),
      request.expressionAttributeValues());
  }

  @Test
  void testGetStatesNoItems() {
    when(client.query(any(QueryRequest.class))).thenReturn(QueryResponse.builder().build());

    Set<String> states = stateTable.getStates(TEST_KEY);
    assertEquals(Set.of(), states);
  }

  @Test
  void testPutState() {
    stateTable.putState(TEST_KEY, TEST_STATE_1);

    ArgumentCaptor<PutItemRequest> argumentCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
    verify(client, times(1)).putItem(argumentCaptor.capture());

    PutItemRequest request = argumentCaptor.getValue();

    assertEquals(TEST_TABLE, request.tableName());
    assertEquals(Map.of("app_key", AttributeValue.fromS(TEST_KEY), "app_state",
        AttributeValue.fromS(TEST_STATE_1)), request.item());
  }
}
