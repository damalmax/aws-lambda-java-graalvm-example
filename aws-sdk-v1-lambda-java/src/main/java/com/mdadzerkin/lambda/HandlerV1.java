package com.mdadzerkin.lambda;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.core.JsonProcessingException;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.mdadzerkin.lambda.model.RequestItem;
import com.mdadzerkin.lambda.repository.RequestItemRepository;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HandlerV1 implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = Logger.getLogger("lambda");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, String> RESPONSE_HEADERS = Map.of(
            "content-type", "application/json",
            "Access-Control-Allow-Headers", "Content-Type",
            "Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Methods", "OPTIONS,POST,GET"
    );
    private static final String QUEUE_NAME = "requestQueue";
    private static final String DYNAMODB_TABLE = "appRequests";

    private static final String LOCALSTACK_HOSTNAME = Optional.ofNullable(System.getenv("LOCALSTACK_HOSTNAME")).orElse("localhost");
    private static final String AWS_REGION = "us-east-1";

    private static final String QUEUED_STATUS = "QUEUED";

    private final AmazonSQS sqsClient;

    private final RequestItemRepository requestItemRepository;

    public HandlerV1() {
        String endpointOverride = String.format("http://%s:4566", LOCALSTACK_HOSTNAME);
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpointOverride, AWS_REGION);
        this.sqsClient = AmazonSQSClientBuilder.standard()
                                               .withEndpointConfiguration(endpointConfiguration)
                                               .build();
        AmazonDynamoDB dynamoDbClient = AmazonDynamoDBClientBuilder.standard()
                                                                   .withEndpointConfiguration(endpointConfiguration)
                                                                   .build();
        this.requestItemRepository = new RequestItemRepository(DYNAMODB_TABLE, dynamoDbClient);
    }

    public HandlerV1(AmazonSQS sqsClient, RequestItemRepository requestItemRepository) {
        this.sqsClient = sqsClient;
        this.requestItemRepository = requestItemRepository;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent req, Context context) {
        try {
            if ("/requests".equals(req.getPath())) {
                if ("POST".equalsIgnoreCase(req.getHttpMethod())) {
                    return handleNewRequest();
                }
                if ("GET".equalsIgnoreCase(req.getHttpMethod())) {
                    return handleListRequests();
                }
            }
        } catch (Exception e) {
            try {
                LOG.log(Level.SEVERE, e, e::getMessage);
                return new APIGatewayProxyResponseEvent()
                        .withHeaders(RESPONSE_HEADERS)
                        .withBody(MAPPER.writeValueAsString(Map.of("error", e.getMessage())))
                        .withIsBase64Encoded(false)
                        .withStatusCode(500);
            } catch (JsonProcessingException ex) {
                return new APIGatewayProxyResponseEvent()
                        .withHeaders(RESPONSE_HEADERS)
                        .withIsBase64Encoded(false)
                        .withStatusCode(500);
            }
        }

        return new APIGatewayProxyResponseEvent()
                .withHeaders(RESPONSE_HEADERS)
                .withIsBase64Encoded(false)
                .withStatusCode(404);
    }

    private APIGatewayProxyResponseEvent handleNewRequest() throws JsonProcessingException {
        String requestId = UUID.randomUUID().toString();

        String queueUrl = sqsClient.getQueueUrl(new GetQueueUrlRequest().withQueueName(QUEUE_NAME)).getQueueUrl();

        SendMessageRequest sqsRequest = new SendMessageRequest()
                .withMessageBody(MAPPER.writeValueAsString(Map.of("requestID", requestId)))
                .withQueueUrl(queueUrl);
        sqsClient.sendMessage(sqsRequest);

        RequestItem item = new RequestItem(UUID.randomUUID().toString(), requestId, new Date().getTime(), QUEUED_STATUS);
        requestItemRepository.save(item);

        return new APIGatewayProxyResponseEvent()
                .withHeaders(RESPONSE_HEADERS)
                .withIsBase64Encoded(false)
                .withBody(MAPPER.writeValueAsString(Map.of(
                        "requestID", requestId,
                        "status", QUEUED_STATUS
                )))
                .withStatusCode(200);
    }

    private APIGatewayProxyResponseEvent handleListRequests() throws JsonProcessingException {
        Collection<RequestItem> items = requestItemRepository.findAll();
        Map<String, Collection<RequestItem>> responseEntity = Map.of(
                "result", items
        );
        return new APIGatewayProxyResponseEvent()
                .withHeaders(RESPONSE_HEADERS)
                .withIsBase64Encoded(false)
                .withBody(MAPPER.writeValueAsString(responseEntity))
                .withStatusCode(200);
    }
}
