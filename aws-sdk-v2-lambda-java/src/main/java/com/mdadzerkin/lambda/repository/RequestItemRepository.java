package com.mdadzerkin.lambda.repository;

import com.mdadzerkin.lambda.model.RequestItem;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.util.Collection;
import java.util.stream.Collectors;

public class RequestItemRepository implements DynamoDbRepository<RequestItem> {
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<RequestItem> table;

    public RequestItemRepository(String tableName, DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.table = dynamoDbEnhancedClient.table(tableName, RequestItem.TABLE_SCHEMA);
    }

    @Override
    public void save(RequestItem requestItem) {
        table.putItem(requestItem);
    }

    @Override
    public Collection<RequestItem> findAll() {
        return table.scan()
                    .items()
                    .stream()
                    .collect(Collectors.toList());
    }
}
