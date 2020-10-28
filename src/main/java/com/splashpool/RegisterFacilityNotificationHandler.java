package com.splashpool;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.splashpool.model.RegisterFacilityNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RegisterFacilityNotificationHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final Logger LOG = LogManager.getLogger(RegisterFacilityNotificationHandler.class);

    private String DB_HOST = System.getenv("DB_HOST");
    private String DB_NAME = System.getenv("DB_NAME");
    private String DB_USER = System.getenv("DB_USER");
    private String DB_PASSWORD = System.getenv("DB_PASSWORD");

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        LOG.info("received: {}", input);

        // Firstly, work out whether we are handling a GET or a POST for users
        String httpMethod = (String) input.get("httpMethod");

        String in_uuid=null;
        Long in_problemId = 0l;
        Object response = null;
        if (httpMethod.equalsIgnoreCase("GET")) {
            if ( input.get("queryStringParameters") != null ) {
                if (((Map) input.get("queryStringParameters")).get("problemId") != null) {
                    in_problemId = Long.parseLong((String) ((Map) input.get("queryStringParameters")).get("problemId"));
                }
            }
            response = getRegisteredNotification(in_problemId);
        } else if (httpMethod.equalsIgnoreCase("POST")) {
            String postBody = (String) input.get("body");
            saveRegisterNotification(postBody);
        } else if (httpMethod.equalsIgnoreCase("DELETE")) {
            if( input.get("queryStringParameters") != null ) {
                if ( ((String) ((Map) input.get("queryStringParameters")).get("uuid"))  != null ) {
                    in_uuid = (String) ((Map) input.get("queryStringParameters")).get("uuid");
                }
                if ( ((Map) input.get("queryStringParameters")).get("problemId") != null ) {
                    in_problemId = Long.parseLong((String) ((Map) input.get("queryStringParameters")).get("problemId"));
                }
                deleteRegisteredNotification(in_uuid, in_problemId);
            }
            // basically, you don't want it to do anything if a uuid/problemId is/are not provided
            // so that's why there is no "else" here.
        }

        // CORS from anywhere
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type");

        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(response)
                .setHeaders(headers)
                .build();
    }


    private void deleteRegisteredNotification(String in_uuid, long in_problemId) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(String.format(
                    "jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement;
            preparedStatement = connection.prepareStatement("Delete from UserRegisteredFacilityNotification where uuid =? and problemId =?");
            preparedStatement.setString(1, in_uuid);
            preparedStatement.setLong(2, in_problemId);

            int rowsDeleted = preparedStatement.executeUpdate();

            LOG.info("{} rows deleted", rowsDeleted);
        }
        catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
    }


    private void saveRegisterNotification(String saveInfo) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;

        try {
            map = mapper.readValue(saveInfo, Map.class);
            Long problemId = ((Number) map.get("problemId")).longValue();
            String uuid    = (String) map.get("uuid");

            LOG.info("we got: {} {}", problemId, uuid);

            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "Insert UserRegisteredFacilityNotification values (?, ?)");
            preparedStatement.setLong(1, problemId);
            preparedStatement.setString(2, uuid);

            int rowsInserted = preparedStatement.executeUpdate();

            LOG.info("{} rows inserted", rowsInserted);
        }
        catch (IOException | ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    private List<RegisterFacilityNotification> getRegisteredNotification(long in_problemId) {
        List<RegisterFacilityNotification> registeredNotifications = new ArrayList<>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(String.format(
                    "jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement;
            if ( in_problemId == 0l ) {
                preparedStatement = connection.prepareStatement("SELECT * from UserRegisteredFacilityNotification order by problemId");
            }
            else {
                preparedStatement = connection.prepareStatement("SELECT * from UserRegisteredFacilityNotification where problemId =? order by problemId, uuid");
                preparedStatement.setLong(1, in_problemId );
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long problemId    = resultSet.getLong("problemId");
                String uuid        = resultSet.getString("uuid");

                LOG.info("RegisteredNotification: {} {}", problemId, uuid);

                registeredNotifications.add(new RegisterFacilityNotification(problemId, uuid));
            }
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }

        return registeredNotifications;
    }
}