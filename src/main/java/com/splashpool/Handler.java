package com.splashpool;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.splashpool.model.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(Handler.class);

	private String DB_HOST = System.getenv("DB_HOST");
	private String DB_NAME = System.getenv("DB_NAME");
	private String DB_USER = System.getenv("DB_USER");
	private String DB_PASSWORD = System.getenv("DB_PASSWORD");

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input);

		// Firstly, work out whether we are handling a GET or a POST for locations
		String httpMethod = (String) input.get("httpMethod");

		String locName=null;
		Object response = null;
		if (httpMethod.equalsIgnoreCase("GET")) {

			if( input.get("queryStringParameters") != null ) {
				locName = (String) ((Map) input.get("queryStringParameters")).get("locationName");
			}
			else {
				LOG.info("QSP is null");
			}

			LOG.info("XXXXXXXXXX locationName gotten is:{}**!!", locName);
			response = getLocations(locName);
		} else if (httpMethod.equalsIgnoreCase("POST")) {
			String postBody = (String) input.get("body");
			LOG.info("We have a POST");
			saveLocation(postBody);
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


	private void saveLocation(String locInfo) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = null;

		try {
			map = mapper.readValue(locInfo, Map.class);
			// As locationId will be auto-generated by the auto_increment we set to null
			Long locId = null;
			String locName = (String) map.get("locationName");
			String locAdd1 = (String) map.get("address1");
			String locAdd2 = (String) map.get("address2");
			String locCity = (String) map.get("city");
			String locPostCode = (String) map.get("postCode");
			String locCountry = (String) map.get("country");
			double locLongitude = (double) map.get("longitude");
			double locLatitude = (double) map.get("latitude");
			String locAdminOrg = (String) map.get("adminOrg");

			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager
					.getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

			PreparedStatement preparedStatement = connection.prepareStatement(
					"Insert Location values (NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			preparedStatement.setString(1, locName);
			preparedStatement.setString(2, locAdd1);
			preparedStatement.setString(3, locAdd2);
			preparedStatement.setString(4, locCity);
			preparedStatement.setString(5, locPostCode);
			preparedStatement.setString(6, locCountry);
			preparedStatement.setDouble(7, locLongitude);
			preparedStatement.setDouble(8, locLatitude);
			preparedStatement.setString(9, locAdminOrg);

			int rowsInserted = preparedStatement.executeUpdate();

			LOG.info("{} rows inserted", rowsInserted);
		}
		catch (IOException | ClassNotFoundException | SQLException e) {
			LOG.error(e.getMessage());
		}
	}

	private List<Location> getLocations(String locName) {
		List<Location> locations = new ArrayList<>();

		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager.getConnection(String.format(
					"jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

			PreparedStatement preparedStatement;
			if ( locName == null ) {
				LOG.info("locName is null");
				preparedStatement = connection.prepareStatement("SELECT * from Location");
			}
			else {
				LOG.info("locName is {}", locName);
				preparedStatement = connection.prepareStatement("SELECT * from Location where locationName =?");
				preparedStatement.setString(1, locName );
			}
			preparedStatement.setQueryTimeout(5);
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Long locationId = resultSet.getLong("locationId");
				String locationName = resultSet.getString("locationName");
				String address1 = resultSet.getString("address1");
				String address2 = resultSet.getString("address2");
				String city = resultSet.getString("city");
				String postCode = resultSet.getString("postCode");
				String country = resultSet.getString("country");
				double longitude = resultSet.getDouble("longitude");
				double latitude = resultSet.getDouble("latitude");
				String adminOrg = resultSet.getString("adminOrg");

				LOG.info("Location: {} {} {} {} {} {} {} {} {} {}", locationId, locationName, address1,
						address2, city, postCode, country, longitude, latitude, adminOrg);

				locations.add(new Location(locationId, locationName, address1, address2, city,
						postCode, country, longitude, latitude, adminOrg));
			}
		} catch (ClassNotFoundException | SQLException e) {
			LOG.error(e.getMessage());
		}

		return locations;
	}
}
