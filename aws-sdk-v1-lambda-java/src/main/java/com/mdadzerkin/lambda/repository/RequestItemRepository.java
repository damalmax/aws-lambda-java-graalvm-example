package com.mdadzerkin.lambda.repository;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.mdadzerkin.lambda.model.RequestItem;

import java.util.ArrayList;
import java.util.Collection;

public class RequestItemRepository implements DynamoDbRepository<RequestItem> {
    private final AmazonDynamoDB amazonDynamoDB;
    private final DynamoDBMapper dynamoDBMapper;

    public RequestItemRepository(String tableName, AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
        DynamoDBMapperConfig.TableNameOverride tableNameOverride = DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName);
        DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
                                                          .withTableNameOverride(tableNameOverride)
                                                          .build();

        this.dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, config);
    }

    @Override
    public void save(RequestItem requestItem) {
        dynamoDBMapper.save(requestItem);
    }

    @Override
    public Collection<RequestItem> findAll() {
        PaginatedScanList<RequestItem> result = dynamoDBMapper.scan(RequestItem.class, new DynamoDBScanExpression());
        result.loadAllResults();
        return new ArrayList<>(result);
    }
}
