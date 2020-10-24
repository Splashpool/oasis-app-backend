package com.splashpool;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.splashpool.model.FacilityProblem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;


public class FacilityProblemHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final Logger LOG = LogManager.getLogger(FacilityProblemHandler.class);

    private String DB_HOST = System.getenv("DB_HOST");
    private String DB_NAME = System.getenv("DB_NAME");
    private String DB_USER = System.getenv("DB_USER");
    private String DB_PASSWORD = System.getenv("DB_PASSWORD");

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        LOG.info("received: {}", input);

        // Firstly, work out whether we are handling a GET or a POST for users
        String httpMethod = (String) input.get("httpMethod");

        Long in_problemId=null;
        Object response = null;
        if (httpMethod.equalsIgnoreCase("GET")) {
            if( input.get("queryStringParameters") != null ) {
                in_problemId = Long.parseLong((String) ((Map) input.get("queryStringParameters")).get("problemId"));
            }
            response = getFacilityProblem(in_problemId);
        }   else if (httpMethod.equalsIgnoreCase("POST")) {
                String postBody = (String) input.get("body");
                saveFacilityProblem(postBody);
        }   else if (httpMethod.equalsIgnoreCase("PUT")) {
                String postBody = (String) input.get("body");
                updateFacilityProblem(postBody);
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



    private void updateFacilityProblem(String saveInfo) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;

        try {
            map = mapper.readValue(saveInfo, Map.class);
            Long problemId          = ((Number) map.get("problemId")).longValue();
            Long locationId         = ((Number) map.get("locationId")).longValue();
            int  version            = (int) map.get("version");
            /* actually, we don't need the auditDateTime field as it will automatically use
               the now() functiont to populate it
            Timestamp auditDateTime = (Long) map.get("auditDateTime");
             */
            String description      = (String) map.get("description");

            LOG.info("we got: {} {} {} {}", problemId, locationId, version, description);

            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            // start a transaction - we need to check if it exists, and if so, what it's latest version
            connection.setAutoCommit(false);

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "Select version from FacilityProblem where problemId =?");
            preparedStatement.setLong(1, problemId);
            ResultSet resultSet = preparedStatement.executeQuery();

            int curVersion=0;
            while (resultSet.next()) {
                curVersion = resultSet.getInt("version");
            }

            // now copy current record into FacilityProblemHistory table
            preparedStatement = connection.prepareStatement(
                    "Insert FacilityProblemHistory ( select * from FacilityProblem where problemId =?)");
            preparedStatement.setLong(1, problemId);
            int rowsInserted = preparedStatement.executeUpdate();
            LOG.info("{} rows inserted into FacilityHistory", rowsInserted);

            // next advance version by 1
            version = curVersion + 1;

            preparedStatement = connection.prepareStatement(
                "Update FacilityProblem set locationId=?, version=?, auditDateTime=now(), description=? " +
                        " where problemId=?");
            preparedStatement.setLong(1, locationId);
            preparedStatement.setInt(2, version);
            preparedStatement.setString(3, description);
            preparedStatement.setLong(4, problemId);

            rowsInserted = preparedStatement.executeUpdate();

            LOG.info("{} rows inserted", rowsInserted);

            // if no errors, lets commit, and reinstate auto Commit
            connection.commit();
            connection.setAutoCommit(true);
        }
        catch (IOException | ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
    }


    private void saveFacilityProblem(String saveInfo) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;

        try {
            map = mapper.readValue(saveInfo, Map.class);
            Long locationId         = ((Number) map.get("locationId")).longValue();
            String description      = (String) map.get("description");

            LOG.info("we got: {} {}", locationId, description);

            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "Insert FacilityProblem values (null, ?, 1, now(), ?)");
            preparedStatement.setLong(1, locationId);
            preparedStatement.setString(2, description);

            int rowsInserted = preparedStatement.executeUpdate();

            LOG.info("{} rows inserted", rowsInserted);
        }
        catch (IOException | ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    private List<FacilityProblem> getFacilityProblem(Long in_problemId) {
        List<FacilityProblem> facilityProblems = new ArrayList<>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(String.format(
                    "jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement;
            if ( in_problemId == null ) {

                preparedStatement = connection.prepareStatement("SELECT * from FacilityProblem");
            }
            else {

                preparedStatement = connection.prepareStatement("SELECT * from FacilityProblem where problemId =?");
                preparedStatement.setLong(1, in_problemId );
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long problemId          = resultSet.getLong("problemId");
                long locationId         = resultSet.getLong("locationId");
                int  version            = resultSet.getInt("version");
                Timestamp auditDateTime = resultSet.getTimestamp("auditDateTime");
                String description = resultSet.getString("description");

                LOG.info("FacilityProblem: {} {} {} {} {}", problemId, locationId, version, auditDateTime, description);

                facilityProblems.add(new FacilityProblem(problemId, locationId, version, auditDateTime, description));
            }
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }

        return facilityProblems;
    }
}
