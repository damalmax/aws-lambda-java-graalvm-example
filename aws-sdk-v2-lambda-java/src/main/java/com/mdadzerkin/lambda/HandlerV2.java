package com.mdadzerkin.lambda;

import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.core.JsonProcessingException;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mdadzerkin.lambda.model.RequestItem;
import com.mdadzerkin.lambda.repository.RequestItemRepository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HandlerV2 implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = Logger.getLogger("lambdaV2");

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

    private static final String QUEUED_STATUS = "QUEUED";

    private final SqsClient sqsClient;

    private final RequestItemRepository requestItemRepository;

    public HandlerV2() {
        URI endpointOverride = URI.create(String.format("http://%s:4566", LOCALSTACK_HOSTNAME));
        this.sqsClient = SqsClient.builder()
                                  .endpointOverride(endpointOverride)
                                  .build();
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                                                      .endpointOverride(endpointOverride)
                                                      .build();
        DynamoDbEnhancedClient dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder()
                                                                              .dynamoDbClient(dynamoDbClient)
                                                                              .build();

        this.requestItemRepository = new RequestItemRepository(DYNAMODB_TABLE, dynamoDbEnhancedClient);
    }

    public HandlerV2(SqsClient sqsClient, RequestItemRepository requestItemRepository) {
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
            LOG.log(Level.SEVERE, e, e::getMessage);
            return new APIGatewayProxyResponseEvent()
                    .withHeaders(RESPONSE_HEADERS)
                    .withStatusCode(500);
        }

        return new APIGatewayProxyResponseEvent()
                .withHeaders(RESPONSE_HEADERS)
                .withStatusCode(404);
    }

    private APIGatewayProxyResponseEvent handleNewRequest() throws JsonProcessingException {
        String requestId = UUID.randomUUID().toString();

        String queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(QUEUE_NAME).build()).queueUrl();

        SendMessageRequest sqsRequest = SendMessageRequest.builder()
                                                          .messageBody(MAPPER.writeValueAsString(Map.of("requestID", requestId)))
                                                          .queueUrl(queueUrl)
                                                          .build();
        sqsClient.sendMessage(sqsRequest);

        RequestItem item = new RequestItem(UUID.randomUUID().toString(), requestId, new Date().getTime(), QUEUED_STATUS);
        requestItemRepository.save(item);

        return new APIGatewayProxyResponseEvent()
                .withHeaders(RESPONSE_HEADERS)
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
                .withBody(MAPPER.writeValueAsString(responseEntity))
                .withStatusCode(200);
    }
}
