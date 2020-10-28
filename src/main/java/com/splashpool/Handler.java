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

		Double longitude = 999.0;
		Double latitude  = 999.0;
		Long   locId=0l;
		Object response  = null;
		if (httpMethod.equalsIgnoreCase("GET")) {
			// The checks below gets longitude, latitude and locationId if they exist.
			// When it goes down to getLocations, the order of preference is:
			// if locationId exist, use it first.   The if longitude & latitude exists, use them
			// otherwise, returns all Locations.
			if( input.get("queryStringParameters") != null ) {
				if ( ( ((Map) input.get("queryStringParameters")).get("longitude") != null ) &&
					 ( ((Map) input.get("queryStringParameters")).get("latitude") != null ) ) {
					longitude = Double.parseDouble((String) ((Map) input.get("queryStringParameters")).get("longitude"));
					latitude = Double.parseDouble((String) ((Map) input.get("queryStringParameters")).get("latitude"));
				}
				if ( ((Map) input.get("queryStringParameters")).get("locationId") != null ) {
					LOG.info("OK, so we are in the right branch");
					locId = Long.parseLong((String) ((Map) input.get("queryStringParameters")).get("locationId"));
					LOG.info("YYYYY {} ", locId);
				}
			}
			response = getLocations(longitude, latitude, locId);
		}
		else if (httpMethod.equalsIgnoreCase("POST")) {
			String postBody = (String) input.get("body");
			saveLocation(postBody);
		}
		else if (httpMethod.equalsIgnoreCase("PUT")) {
			String postBody = (String) input.get("body");
			updateLocation(postBody);
		}
		else if (httpMethod.equalsIgnoreCase("DELETE")) {
			Long locationId = Long.parseLong((String) ((Map) input.get("queryStringParameters")).get("locationId"));
			deleteLocation(locationId);
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


	private void deleteLocation(Long in_locationId) {

		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager.getConnection(String.format(
					"jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));
			// need to set autoCommit to false as we want to do a transaction block
			connection.setAutoCommit(false);

			// need to delete from Comment table as it has a foreign key constraint on the Location.locationId
			PreparedStatement preparedStatement;
			preparedStatement = connection.prepareStatement("Delete from Comment where locationId =?");
			preparedStatement.setLong(1, in_locationId);

			// at this stage, we don't care if it deletes nothing, it's not an error if there's nothing to delete.
			int rowsDeleted = preparedStatement.executeUpdate();
			LOG.info("{} rows deleted from Comment for {}", rowsDeleted, in_locationId);

			// now delete the Location table since we've deleted those which have a foreign key constraint.
			preparedStatement = connection.prepareStatement("Delete from Location where locationId =?");
			preparedStatement.setLong(1, in_locationId);

			rowsDeleted = preparedStatement.executeUpdate();
			LOG.info("{} rows deleted from Location for {}", rowsDeleted, in_locationId);

			// no errors, lets commit the changes and reinstate auto Commit
			connection.commit();
			connection.setAutoCommit(true);
		}
		catch (ClassNotFoundException | SQLException e) {
			LOG.error(e.getMessage());
		}
	}



	private void updateLocation(String locInfo) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = null;

		try {
			map = mapper.readValue(locInfo, Map.class);
			// thing to remember is that ObjectMapper or Map, whenever the json goes
			// through deserialization or an untyped stage, you need to write code like
			// below to get a Long from the json (because a figure like 100001, because it's within
			// 32bits, would automatically convert to Integer).  You have to call it like below
			// to get it as a Long.
			Long locId = ((Number) map.get("locationId")).longValue();
			String locName = (String) map.get("locationName");
			String locAdd1 = (String) map.get("address1");
			String locAdd2 = (String) map.get("address2");
			String locCity = (String) map.get("city");
			String locPostCode = (String) map.get("postCode");
			String locCountry = (String) map.get("country");
			double locLongitude = (double) map.get("longitude");
			double locLatitude = (double) map.get("latitude");
			String locAdminOrg = (String) map.get("adminOrg");
			boolean locWater = (boolean) map.get("water");
			boolean locDrinkable = (boolean) map.get("drinkable");
			boolean locTreatment = (boolean) map.get("treatment");
			boolean locUnknown = (boolean) map.get("unknown");
			boolean locLargeWaterFacility = (boolean) map.get("largeWaterFacility");
			boolean locMaleToilets = (boolean) map.get("maleToilets");
			boolean locFemaleToilets = (boolean) map.get("femaleToilets");
			boolean locLargeToiletFacility = (boolean) map.get("largeToiletFacility");
			boolean locDisabledAccess = (boolean) map.get("disabledAccess");
			boolean locChargeForUse = (boolean) map.get("chargeForUse");
			String  locOpeningHours = (String) map.get("openingHours");
			boolean locHasIssue = (boolean) map.get("hasIssue");


			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager
					.getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

			PreparedStatement preparedStatement = connection.prepareStatement(
					"Update Location set locationName=?, address1=?, address2=?, city=?, postCode=?," +
							" country=?, longitude=?, latitude=?, adminOrg=?, water=?, drinkable=?," +
							" treatment=?, unknown=?, largeWaterFacility=?, maleToilets=?, femaleToilets=?," +
							" largeToiletFacility=?, disabledAccess=?, chargeForUse=?, openingHours=?, hasIssue=?" +
							" where locationId=?");
			preparedStatement.setString(1, locName);
			preparedStatement.setString(2, locAdd1);
			preparedStatement.setString(3, locAdd2);
			preparedStatement.setString(4, locCity);
			preparedStatement.setString(5, locPostCode);
			preparedStatement.setString(6, locCountry);
			preparedStatement.setDouble(7, locLongitude);
			preparedStatement.setDouble(8, locLatitude);
			preparedStatement.setString(9, locAdminOrg);
			preparedStatement.setBoolean(10, locWater);
			preparedStatement.setBoolean(11, locDrinkable);
			preparedStatement.setBoolean(12, locTreatment);
			preparedStatement.setBoolean(13, locUnknown);
			preparedStatement.setBoolean(14, locLargeWaterFacility);
			preparedStatement.setBoolean(15, locMaleToilets);
			preparedStatement.setBoolean(16, locFemaleToilets);
			preparedStatement.setBoolean(17, locLargeToiletFacility);
			preparedStatement.setBoolean(18, locDisabledAccess);
			preparedStatement.setBoolean(19, locChargeForUse);
			preparedStatement.setString(20, locOpeningHours);
			preparedStatement.setBoolean(21, locHasIssue);
			preparedStatement.setLong(22, locId);

			int rowsUpdated = preparedStatement.executeUpdate();

			LOG.info("{} rows inserted", rowsUpdated);
		}
		catch (IOException | ClassNotFoundException | SQLException e) {
			LOG.error(e.getMessage());
		}
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
			double locLongitude = 0.0000000000000001;  // setting default value for if not entered.
			if ( map.get("longitude") != null ) {
				locLongitude = (double) map.get("longitude");
			}
			else { locLongitude = 0.0000000000000001; }
			double locLatitude = 0.0000000000000001;	// setting default value for if not entered.
			if ( map.get("latitude") != null ) {
				locLatitude = (double) map.get("latitude");
			}
			else { locLatitude = 0.0000000000000001; }
			String locAdminOrg = (String) map.get("adminOrg");
			boolean locWater = (boolean) map.get("water");
			boolean locDrinkable = (boolean) map.get("drinkable");
			boolean locTreatment = (boolean) map.get("treatment");
			boolean locUnknown = (boolean) map.get("unknown");
			boolean locLargeWaterFacility = (boolean) map.get("largeWaterFacility");
			boolean locMaleToilets = (boolean) map.get("maleToilets");
			boolean locFemaleToilets = (boolean) map.get("femaleToilets");
			boolean locLargeToiletFacility = (boolean) map.get("largeToiletFacility");
			boolean locDisabledAccess = (boolean) map.get("disabledAccess");
			boolean locChargeForUse = (boolean) map.get("chargeForUse");
			String  locOpeningHours = (String) map.get("openingHours");
			boolean locHasIssue = (boolean) map.get("hasIssue");


			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager
					.getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

			// The following preparedStatement - only because java can't initialize a double with null.
			// and we have locLongitude and locLatitude and their combinations to think about.
			PreparedStatement preparedStatement;
			if ( locLongitude == 0.0000000000000001 && locLatitude == 0.0000000000000001 ) {
				preparedStatement = connection.prepareStatement(
						"Insert Location values (NULL, ?, ?, ?, ?, ?, ?, NULL, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			}
			else if ( locLongitude == 0.0000000000000001 ) {
				preparedStatement = connection.prepareStatement(
						"Insert Location values (NULL, ?, ?, ?, ?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			}
			else if ( locLatitude == 0.0000000000000001 ){
				preparedStatement = connection.prepareStatement(
						"Insert Location values (NULL, ?, ?, ?, ?, ?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			}
			else {
				preparedStatement = connection.prepareStatement(
						"Insert Location values (NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			}
			int colIndex = 0;
			preparedStatement.setString(++colIndex, locName);
			preparedStatement.setString(++colIndex, locAdd1);
			preparedStatement.setString(++colIndex, locAdd2);
			preparedStatement.setString(++colIndex, locCity);
			preparedStatement.setString(++colIndex, locPostCode);
			preparedStatement.setString(++colIndex, locCountry);
			if ( locLongitude != 0.0000000000000001 ) {
				preparedStatement.setDouble(++colIndex, locLongitude);
			}
			if ( locLatitude != 0.0000000000000001 ) {
				preparedStatement.setDouble(++colIndex, locLatitude);
			}
			preparedStatement.setString(++colIndex, locAdminOrg);
			preparedStatement.setBoolean(++colIndex, locWater);
			preparedStatement.setBoolean(++colIndex, locDrinkable);
			preparedStatement.setBoolean(++colIndex, locTreatment);
			preparedStatement.setBoolean(++colIndex, locUnknown);
			preparedStatement.setBoolean(++colIndex, locLargeWaterFacility);
			preparedStatement.setBoolean(++colIndex, locMaleToilets);
			preparedStatement.setBoolean(++colIndex, locFemaleToilets);
			preparedStatement.setBoolean(++colIndex, locLargeToiletFacility);
			preparedStatement.setBoolean(++colIndex, locDisabledAccess);
			preparedStatement.setBoolean(++colIndex, locChargeForUse);
			preparedStatement.setString(++colIndex, locOpeningHours);
			preparedStatement.setBoolean(++colIndex, locHasIssue);

			int rowsInserted = preparedStatement.executeUpdate();

			LOG.info("{} rows inserted", rowsInserted);
		}
		catch (IOException | ClassNotFoundException | SQLException e) {
			LOG.error(e.getMessage());
		}
	}

//	private List<Location> getLocations(String locName) {
	private List<Location> getLocations(double in_longitude, double in_latitude, long in_locationId) {
		List<Location> locations = new ArrayList<>();

		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager.getConnection(String.format(
					"jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

			PreparedStatement preparedStatement;
			if ( in_locationId != 0 ) {
				preparedStatement = connection.prepareStatement("Select * from Location where locationId =?");
				preparedStatement.setLong(1, in_locationId);
			}
			else if( in_longitude == 999.0 ) {
				preparedStatement = connection.prepareStatement("Select * from Location");
			}
			else {
				double lowerLongitude = in_longitude - 2.0;
				double upperLongitude = in_longitude + 2.0;
				double lowerLatitude = in_latitude - 1.0;
				double upperLatitude = in_latitude + 1.0;
				preparedStatement = connection.prepareStatement("SELECT * from Location where " +
						" ( longitude between ? and ? ) and ( latitude between ? and ? )");
				preparedStatement.setDouble(1, lowerLongitude);
				preparedStatement.setDouble(2, upperLongitude);
				preparedStatement.setDouble(3, lowerLatitude);
				preparedStatement.setDouble(4, upperLatitude);
			}

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Long    locationId = resultSet.getLong("locationId");
				String  locationName = resultSet.getString("locationName");
				String  address1 = resultSet.getString("address1");
				String  address2 = resultSet.getString("address2");
				String  city = resultSet.getString("city");
				String  postCode = resultSet.getString("postCode");
				String  country = resultSet.getString("country");
				double  longitude = resultSet.getDouble("longitude");
				double  latitude = resultSet.getDouble("latitude");
				String  adminOrg = resultSet.getString("adminOrg");
				boolean water = resultSet.getBoolean("water");
				boolean drinkable = resultSet.getBoolean("drinkable");
				boolean treatment = resultSet.getBoolean("treatment");
				boolean unknown = resultSet.getBoolean("unknown");
				boolean largeWaterFacility = resultSet.getBoolean("largeWaterFacility");
				boolean maleToilets = resultSet.getBoolean("maleToilets");
				boolean femaleToilets = resultSet.getBoolean("femaleToilets");
				boolean largeToiletFacility = resultSet.getBoolean("largeToiletFacility");
				boolean disabledAccess = resultSet.getBoolean("disabledAccess");
				boolean chargeForUse = resultSet.getBoolean("chargeForUse");
				String  openingHours = resultSet.getString("openingHours");
				boolean hasIssue = resultSet.getBoolean("hasIssue");

				LOG.info("Location: {} {} {} {} {} {} {} {} {} {} {} {} {} {} {} {} {} {} {} {} {} {}",
						locationId, locationName, address1, address2, city, postCode, country,
						longitude, latitude, adminOrg, water, drinkable, treatment, unknown,
						largeWaterFacility, maleToilets, femaleToilets, largeToiletFacility,
						disabledAccess, chargeForUse, openingHours, hasIssue);

				locations.add(new Location(locationId, locationName, address1, address2, city,
						postCode, country, longitude, latitude, adminOrg, water, drinkable,
						treatment, unknown, largeWaterFacility, maleToilets, femaleToilets,
						largeToiletFacility, disabledAccess, chargeForUse, openingHours, hasIssue));
			}
		} catch (ClassNotFoundException | SQLException e) {
			LOG.error(e.getMessage());
		}

		return locations;
	}
}
