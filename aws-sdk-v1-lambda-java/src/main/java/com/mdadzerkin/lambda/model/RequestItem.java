package com.mdadzerkin.lambda.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;

import java.util.Objects;

@DynamoDBDocument
public class RequestItem implements Entity {
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_REQUEST_ID = "requestID";
    private static final String ATTRIBUTE_TIMESTAMP = "timestamp";
    private static final String ATTRIBUTE_STATUS = "status";

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

    @DynamoDBHashKey(attributeName = ATTRIBUTE_ID)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = ATTRIBUTE_REQUEST_ID)
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @DynamoDBAttribute(attributeName = ATTRIBUTE_TIMESTAMP)
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @DynamoDBAttribute(attributeName = ATTRIBUTE_STATUS)
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
