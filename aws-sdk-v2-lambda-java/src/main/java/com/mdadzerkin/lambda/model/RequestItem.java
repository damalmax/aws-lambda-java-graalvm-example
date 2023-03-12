package com.mdadzerkin.lambda.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.Objects;

@DynamoDbBean
public class RequestItem implements Entity {
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_REQUEST_ID = "requestID";
    private static final String ATTRIBUTE_TIMESTAMP = "timestamp";
    private static final String ATTRIBUTE_STATUS = "status";
    public static final TableSchema<RequestItem> TABLE_SCHEMA = StaticTableSchema.builder(RequestItem.class)
                                                                                 .newItemSupplier(RequestItem::new)
                                                                                 .addAttribute(String.class, a -> a.name(ATTRIBUTE_ID)
                                                                                                                   .getter(RequestItem::getId)
                                                                                                                   .setter(RequestItem::setId)
                                                                                                                   .tags(StaticAttributeTags.primaryPartitionKey()))
                                                                                 .addAttribute(String.class, a -> a.name(ATTRIBUTE_REQUEST_ID)
                                                                                                                   .getter(RequestItem::getRequestId)
                                                                                                                   .setter(RequestItem::setRequestId))
                                                                                 .addAttribute(Long.class, a -> a.name(ATTRIBUTE_TIMESTAMP)
                                                                                                                 .getter(RequestItem::getTimestamp)
                                                                                                                 .setter(RequestItem::setTimestamp))
                                                                                 .addAttribute(String.class, a -> a.name(ATTRIBUTE_STATUS)
                                                                                                                   .getter(RequestItem::getStatus)
                                                                                                                   .setter(RequestItem::setStatus))
                                                                                 .build();
    private String id;
    private String requestId;
    private Long timestamp;
    private String status;

    public RequestItem() {
    }

    public RequestItem(String id, String requestId, Long timestamp, String status) {
        this.id = id;
        this.requestId = requestId;
        this.timestamp = timestamp;
        this.status = status;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute(ATTRIBUTE_ID)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDbAttribute(ATTRIBUTE_REQUEST_ID)
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @DynamoDbAttribute(ATTRIBUTE_TIMESTAMP)
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @DynamoDbAttribute(ATTRIBUTE_STATUS)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestItem that = (RequestItem) o;
        return Objects.equals(id, that.id)
                && Objects.equals(requestId, that.requestId)
                && Objects.equals(timestamp, that.timestamp)
                && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, requestId, timestamp, status);
    }

    @Override
    public String toString() {
        return "RequestItem{" +
                "id='" + id + '\'' +
                ", requestId='" + requestId + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}
