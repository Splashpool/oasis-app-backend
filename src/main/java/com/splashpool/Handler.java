package com.splashpool;

import java.util.Collections;
import java.util.Map;
import com.splashpool.model.Location;
import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(Handler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input);

		Location l1 = new Location("London", "A123345");
		Location l2 = new Location("Birmingham", "A56789");

		List<Location> locations = new ArrayList<>();
		locations.add(l1);
		locations.add(l2);


		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(locations)
				.build();
	}
}
