package com.splashpool;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.splashpool.model.SavedLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SavedLocationHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final Logger LOG = LogManager.getLogger(SavedLocationHandler.class);

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
        Object response = null;
        if (httpMethod.equalsIgnoreCase("GET")) {
            if( input.get("queryStringParameters") != null ) {
                in_uuid = (String) ((Map) input.get("queryStringParameters")).get("uuid");
            }
            response = getSavedLocation(in_uuid);
        } else if (httpMethod.equalsIgnoreCase("POST")) {
            String postBody = (String) input.get("body");
            saveSavedLocation(postBody);
        } else if (httpMethod.equalsIgnoreCase("DELETE")) {
            if( input.get("queryStringParameters") != null ) {
                if ( ((String) ((Map) input.get("queryStringParameters")).get("uuid"))  != null ) {
                    in_uuid = (String) ((Map) input.get("queryStringParameters")).get("uuid");
                    deleteSavedLocation(in_uuid);
                }
            }
            // basically, you don't want it to do anything if a uuid is not provided
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


    private void deleteSavedLocation(String in_uuid) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(String.format(
                    "jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement;
            preparedStatement = connection.prepareStatement("Delete from SavedLocation where uuid =?");
            preparedStatement.setString(1, in_uuid);

            int rowsDeleted = preparedStatement.executeUpdate();

            LOG.info("{} rows deleted", rowsDeleted);
        }
        catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
    }


    private void saveSavedLocation(String saveInfo) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;

        try {
            map = mapper.readValue(saveInfo, Map.class);
            Long locationId     = ((Number) map.get("locationId")).longValue();
            String uuid         = (String) map.get("uuid");

            LOG.info("we got: {} {}", locationId, uuid);

            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "Insert SavedLocation values (?, ?)");
            preparedStatement.setLong(1, locationId);
            preparedStatement.setString(2, uuid);

            int rowsInserted = preparedStatement.executeUpdate();

            LOG.info("{} rows inserted", rowsInserted);
        }
        catch (IOException | ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    private List<SavedLocation> getSavedLocation(String in_uuid) {
        List<SavedLocation> savedLocations = new ArrayList<>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(String.format(
                    "jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement;
            if ( in_uuid == null ) {
                preparedStatement = connection.prepareStatement("SELECT * from SavedLocation");
            }
            else {
                preparedStatement = connection.prepareStatement("SELECT * from SavedLocation where uuid =?");
                preparedStatement.setString(1, in_uuid );
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long locationId    = resultSet.getLong("locationId");
                String uuid        = resultSet.getString("uuid");

                LOG.info("SavedLocation: {} {}", locationId, uuid);

                savedLocations.add(new SavedLocation(locationId, uuid));
            }
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }

        return savedLocations;
    }
}
