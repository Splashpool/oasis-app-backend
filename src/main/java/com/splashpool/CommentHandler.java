package com.splashpool;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.splashpool.model.Comment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CommentHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final Logger LOG = LogManager.getLogger(CommentHandler.class);

    private String DB_HOST = System.getenv("DB_HOST");
    private String DB_NAME = System.getenv("DB_NAME");
    private String DB_USER = System.getenv("DB_USER");
    private String DB_PASSWORD = System.getenv("DB_PASSWORD");

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        LOG.info("received: {}", input);

        // Firstly, work out whether we are handling a GET or a POST for users
        String httpMethod = (String) input.get("httpMethod");

        Long in_locationId = 0l;
        Object response = null;
        if (httpMethod.equalsIgnoreCase("GET")) {
            if (input.get("queryStringParameters") != null) {
                in_locationId = Long.parseLong((String) ((Map) input.get("queryStringParameters")).get("locationId"));
            }
            response = getComment(in_locationId);
        } else if (httpMethod.equalsIgnoreCase("POST")) {
            String postBody = (String) input.get("body");
            saveComment(postBody);
        } else if (httpMethod.equalsIgnoreCase("PUT")) {
            String postBody = (String) input.get("body");
            updateComment(postBody);
        } else if (httpMethod.equalsIgnoreCase("DELETE")) {
            if (input.get("queryStringParameters") != null) {
                in_locationId = Long.parseLong((String) ((Map) input.get("queryStringParameters")).get("locationId"));
            }
            if ( in_locationId != 0l ) {
                deleteComment(in_locationId);
            }
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


    private void updateComment(String commentInfo) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;

        try {
            map = mapper.readValue(commentInfo, Map.class);

            Long   locId      = ((Number) map.get("locationId")).longValue();
            String comment    = (String)  map.get("comment");
            String pictureURL = (String)  map.get("pictureURL");
            int    rating     = (int)     map.get("rating");

            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "Update Comment set comment=?, pictureUrl=?, rating=? where locationId=?" );
            preparedStatement.setString(1, comment);
            preparedStatement.setString(2, pictureURL);
            preparedStatement.setInt(3, rating);
            preparedStatement.setLong(4, locId);

            int rowsUpdated = preparedStatement.executeUpdate();

            LOG.info("{} rows updated", rowsUpdated);
        }
        catch (IOException | ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
    }


    private void saveComment(String commentInfo) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;

        try {
            map = mapper.readValue(commentInfo, Map.class);

            Long   locId      = ((Number) map.get("locationId")).longValue();
            String comment    = (String)  map.get("comment");
            String pictureURL = (String)  map.get("pictureURL");
            int    rating     = (int)     map.get("rating");

            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "Insert Comment values (?, ?, ?, ?)" );
            preparedStatement.setLong(1, locId);
            preparedStatement.setString(2, comment);
            preparedStatement.setString(3, pictureURL);
            preparedStatement.setInt(4, rating);

            int rowsInserted = preparedStatement.executeUpdate();

            LOG.info("{} rows inserted", rowsInserted);
        }
        catch (IOException | ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
    }


    private void deleteComment(Long in_locationId) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(String.format(
                    "jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement;
            preparedStatement = connection.prepareStatement("Delete from Comment where locationId =?");
            preparedStatement.setLong(1, in_locationId);

            int rowsDeleted = preparedStatement.executeUpdate();
            LOG.info("{} rows deleted from Comment for {}", rowsDeleted, in_locationId);

        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
    }


    private List<Comment> getComment(Long in_locationId) {
        List<Comment> comments = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(String.format(
                    "jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from Comment where locationId =?");
            preparedStatement.setLong(1, in_locationId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Long locationId   = resultSet.getLong("locationId");
                String comment    = resultSet.getString("comment");
                String pictureURL = resultSet.getString("pictureURL");
                int rating        = resultSet.getInt("rating");

                LOG.info("Comment: {} {} {} {}", locationId, comment, pictureURL, rating);
                comments.add(new Comment(locationId, comment, pictureURL, rating));
            }
        }
        catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }

        return comments;
    }
}