package com.splashpool;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.splashpool.model.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(Handler.class);

	private String DB_HOST = System.getenv("DB_HOST");
	private String DB_NAME = System.getenv("DB_NAME");
	private String DB_USER = System.getenv("DB_USER");
	private String DB_PASSWORD = System.getenv("DB_PASSWORD");

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input);

		// CORS from anywhere
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Allow-Headers", "Content-Type");

//		String userId = (String) ((Map)input.get("queryStringParameters")).get("userId");

		List<Location> locations = new ArrayList<>();

		try {
			Class.forName("com.mysql.jdbc.Driver");

			Connection connect = DriverManager.getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

			Statement statement = connect.createStatement();
			ResultSet resultSet = statement.executeQuery("select locationName, locationId from oasis.Location");

//			 Connection connection = DriverManager.getConnection(String.format(
//			 		"jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));
//			 PreparedStatement preparedStatement = connection.prepareStatement(
//					"SELECT locationName, locationId from Location where LocationName =?");
//			 preparedStatement.setString(1, userId);
//			 ResultSet resultSet = preparedStatement.executeQuery();



			while( resultSet.next()) {
				String locationName = resultSet.getString("locationName");
				long locationId = resultSet.getLong("locationId");

				LOG.info("User: {} {}", locationName, locationId);

				locations.add(new Location(locationName, locationId));
			}

		} catch (ClassNotFoundException | SQLException e) {
			LOG.error(e.getMessage());
		}

		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(locations)
				.setHeaders(headers)
				.build();
	}
}
