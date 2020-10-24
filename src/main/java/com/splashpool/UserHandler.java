package com.splashpool;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.splashpool.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final Logger LOG = LogManager.getLogger(UserHandler.class);

    private String DB_HOST = System.getenv("DB_HOST");
    private String DB_NAME = System.getenv("DB_NAME");
    private String DB_USER = System.getenv("DB_USER");
    private String DB_PASSWORD = System.getenv("DB_PASSWORD");

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        LOG.info("received: {}", input);

        // Firstly, work out whether we are handling a GET or a POST for users
        String httpMethod = (String) input.get("httpMethod");

        String in_email=null;
        Object response = null;
        if (httpMethod.equalsIgnoreCase("GET")) {

            if( input.get("queryStringParameters") != null ) {
                in_email = (String) ((Map) input.get("queryStringParameters")).get("email");
            }

            response = getUser(in_email);
        } else if (httpMethod.equalsIgnoreCase("POST")) {
            String postBody = (String) input.get("body");
            saveUser(postBody);
        } else if (httpMethod.equalsIgnoreCase("DELETE")) {
            in_email = (String) ((Map) input.get("queryStringParameters")).get("email");
            deleteUser(in_email);
        } else if (httpMethod.equalsIgnoreCase("PUT")) {
            String postBody = (String) input.get("body");
            updateUser(postBody);
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


    private void updateUser(String userInfo) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;

        try {
            map = mapper.readValue(userInfo, Map.class);
            String email        = (String) map.get("email");
            String uuid         = (String) map.get("uuid");
            String firstName    = (String) map.get("firstName");
            String lastName     = (String) map.get("lastName");
            String countryCode  = (String) map.get("countryCode");
            String mobileNumber = (String) map.get("mobileNumber");
            boolean adminUser   = (boolean) map.get("adminUser");
            String organisation = (String) map.get("organisation");
            String orgAddress1  = (String) map.get("orgAddress1");
            String orgAddress2  = (String) map.get("orgAddress2");
            String orgCity      = (String) map.get("orgCity");
            String orgPostCode  = (String) map.get("orgPostCode");
            String orgCountry   = (String) map.get("orgCountry");

            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "Update User set firstName=?, lastName=?, countryCode=?, mobileNumber=?, adminUser=?, organisation=?, orgAddress1=?, orgAddress2=?, orgCity=?, orgPostCode=?, orgCountry=? where email= ?");
            // We cannot set email or uuid as they are linked, and unique to the user, and are the key
            // driving the statements.
//            preparedStatement.setString(1, email);
//            preparedStatement.setString(2, uuid);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, countryCode);
            preparedStatement.setString(4, mobileNumber);
            preparedStatement.setBoolean(5, adminUser);
            preparedStatement.setString(6, organisation);
            preparedStatement.setString(7, orgAddress1);
            preparedStatement.setString(8, orgAddress2);
            preparedStatement.setString(9, orgCity);
            preparedStatement.setString(10, orgPostCode);
            preparedStatement.setString(11, orgCountry);
            preparedStatement.setString(12, email);

            int rowsUpdated = preparedStatement.executeUpdate();

            LOG.info("{} rows updated", rowsUpdated);
        }
        catch (IOException | ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
    }


    private void deleteUser(String in_email) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(String.format(
                    "jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));
            // need to set autoCommit to false as we want to do a transaction block
            connection.setAutoCommit(false);

            PreparedStatement preparedStatement;
            preparedStatement = connection.prepareStatement("Delete from SavedLocation where uuid = " +
                    "(select uuid from User where email =?)");
            preparedStatement.setString(1, in_email);

            // at this stage, we don't care if it deletes nothing, it's not an error if there's nothing to delete.
            int rowsDeleted = preparedStatement.executeUpdate();
            LOG.info("{} rows deleted from SavedLocation for {}", rowsDeleted, in_email);

            // now delete the User table since we've deleted those which have a foreign key constraint.
            preparedStatement = connection.prepareStatement("Delete from User where email =?");
            preparedStatement.setString(1, in_email);

            rowsDeleted = preparedStatement.executeUpdate();
            LOG.info("{} rows deleted from User for {}", rowsDeleted, in_email);

            // no errors, lets commit the changes and reinstate auto Commit
            connection.commit();
            connection.setAutoCommit(true);
        }
        catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    private void saveUser(String userInfo) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;

        try {
            map = mapper.readValue(userInfo, Map.class);
            String email        = (String) map.get("email");
            String uuid         = (String) map.get("uuid");
            String firstName    = (String) map.get("firstName");
            String lastName     = (String) map.get("lastName");
            String countryCode  = (String) map.get("countryCode");
            String mobileNumber = (String) map.get("mobileNumber");
            boolean adminUser   = (boolean) map.get("adminUser");
            String organisation = (String) map.get("organisation");
            String orgAddress1  = (String) map.get("orgAddress1");
            String orgAddress2  = (String) map.get("orgAddress2");
            String orgCity      = (String) map.get("orgCity");
            String orgPostCode  = (String) map.get("orgPostCode");
            String orgCountry   = (String) map.get("orgCountry");

            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "Insert User values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, uuid);
            preparedStatement.setString(3, firstName);
            preparedStatement.setString(4, lastName);
            preparedStatement.setString(5, countryCode);
            preparedStatement.setString(6, mobileNumber);
            preparedStatement.setBoolean(7, adminUser);
            preparedStatement.setString(8, organisation);
            preparedStatement.setString(9, orgAddress1);
            preparedStatement.setString(10, orgAddress2);
            preparedStatement.setString(11, orgCity);
            preparedStatement.setString(12, orgPostCode);
            preparedStatement.setString(13, orgCountry);

            int rowsInserted = preparedStatement.executeUpdate();

            LOG.info("{} rows inserted", rowsInserted);
        }
        catch (IOException | ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    private List<User> getUser(String in_email) {
        List<User> users = new ArrayList<>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(String.format(
                    "jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            PreparedStatement preparedStatement;
            if ( in_email == null ) {
                preparedStatement = connection.prepareStatement("SELECT * from User");
            }
            else {
                preparedStatement = connection.prepareStatement("SELECT * from User where email =?");
                preparedStatement.setString(1, in_email );
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String email        = resultSet.getString("email");
                String uuid         = resultSet.getString("uuid");
                String firstName    = resultSet.getString("firstName");
                String lastName     = resultSet.getString("lastName");
                String countryCode  = resultSet.getString("countryCode");
                String mobileNumber = resultSet.getString("mobileNumber");
                boolean adminUser   = resultSet.getBoolean("adminUser");
                String organisation = resultSet.getString("organisation");
                String orgAddress1  = resultSet.getString("orgAddress1");
                String orgAddress2  = resultSet.getString("orgAddress2");
                String orgCity      = resultSet.getString("orgCity");
                String orgPostCode  = resultSet.getString("orgPostCode");
                String orgCountry   = resultSet.getString("orgCountry");

                LOG.info("User: {} {} {} {} {} {} {} {} {} {} {} {} {}", email, uuid,
                        firstName, lastName, countryCode, mobileNumber, adminUser,
                        organisation, orgAddress1, orgAddress2, orgCity, orgPostCode, orgCountry);

                users.add(new User(email, uuid, firstName, lastName, countryCode, mobileNumber, adminUser,
                        organisation, orgAddress1, orgAddress2, orgCity, orgPostCode, orgCountry));
            }
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }

        return users;
    }
}
