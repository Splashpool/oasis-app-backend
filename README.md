# Oasis App Backend

## Introduction

This is the back end API of the Oasis application written by Team Splashpool during the [Tech Returners](https://techreturners.com) Your Return to Tech programme in London Sept-Oct 2020 inclusive.
It is a serverless framework running lambda functions, driven by a React front end application.
The code base is [here](https://github.com/Splashpool/oasis-app-backend.git).
The back end connects to an AWS RDS (MySQL) database.

The hosted front end of the application is [here](https://splashpool.github.io/).


### Technology used

- java with maven
- Serverless Framework
- AWS Lambda and API Gateway
- AWS RDS
- SQL for MySQL DB

### Endpoints
The Oasis app provides the following endpoints to the front end.

---
##### GET /locations
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/locations](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/locations)
Returns JSON object containing a list of (Facility) Locations in the database.
List depends on input parameters passed in: locationId, longitude & latitude.

If all 3 parameters are provided (or just locationId), it will query Location by locationId.

If location is not provided but longitude and latitude are, then it will query by longitude & latitude.

If the 3 are not provided, it will return all Locations.


##### GET /users
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/users](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/users)
Returns JSON object containing either a single User record or all the User records in the User table depending on whether an email was passed in as an input parameter.


##### GET /savedLocations
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/savedLocations](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/savedLocations)
Returns JSON object containing a list of an individual user's saved locations - so a user may query a map location for facilities, and save this location as a place of interest.

If no user id is provided, will return all saved locations for all users. (more for admin/maintenance)


##### GET /facilityProblems
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/facilityProblems](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/facilityProblems)
Returns JSON object containing a list of records of Facilities that had problem(s) and or a full history of the problem.

If a problemId is passed in, then only a specific Facility's problem (and/or its history) will be returned.


##### GET /registerNotifications
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/registerNotifications](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/registerNotifications)
Returns JSON object containing the Facilities where users have registered interest in them -for example if the problem was reported to have an issue or some problem.

If no arguments are passed in, then it returns all registeredNotifications.
If a problemId is passed in, then only return those with the same problemId.


---

##### POST /locations
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/locations](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/locations)
Will create a new Location in the DB when sent a JSON payload in the format:

```
{
    "locationId": 300003,
    "locationName": "Wimbledon",
    "address1": "26 Lyveden Road",
    "address2": null,
    "city": "London",
    "postCode": "SW17 9DU",
    "country": "UK",
    "longitude": -0.16698,
    "latitude": 51.419942,
    "adminOrg": "Unicef",
    "water": true,
    "drinkable": true,
    "treatment": false,
    "unknown": false,
    "largeWaterFacility": false,
    "maleToilets": true,
    "femaleToilets": true,
    "largeToiletFacility": false,
    "disabledAccess": false,
    "chargeForUse": false,
    "openingHours": "M-F 24hr, Sat 10-21:00, Sun 11-18:00",
    "hasIssue": true,
    "comment": null,
    "pictureURL": "https://oasis-images.s3.eu-west-2.amazonaws.com/location-images/300003.PNG",
    "rating": 5
}
```

##### POST /users
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/users](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/users)
Will create a new User in the DB when sent a JSON payload in the format:

```
{
    "email": "joe.blogs@yrtt.com",
    "uuid": "joeblogs_uuid",
    "firstName": "Joe",
    "lastName": "Blogs",
    "countryCode": "44",
    "mobileNumber": "07887345345",
    "adminUser": true,
    "organisation": "Joe's Charity",
    "orgAddress1": "27 Primrose Hill",
    "orgAddress2": "",
    "orgCity": "London",
    "orgPostCode": "NW1 6XL",
    "orgCountry": "UK"
    }
```

##### POST /savedLocations
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/savedLocations](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/savedLocations)
Will create a new SavedLocation in the DB when sent a JSON payload in the format:

```
{
    "locationId": 600000,
    "uuid": "frankb_uuid"
}
```

##### POST  /facilityProblems
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/facilityProblems](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/facilityProblems)
Will create a new FacilityProblem in the DB when sent a JSON payload in the format:

```
{
    "problemId": 200003,
    "locationId": 300004,
    "version": 2,
    "auditDateTime": 1603502928000,
    "description": "Another PUT test FacilityProblem"
}
```


##### POST /registerNotifications
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/registerNotifications](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/registerNotifications)
Will create a UserRegisteredFacilityNotification in the DB when sent a JSON payload in the format:

```
{
    "problemId": 200001,
    "uuid": "eva_uuid"
}
```

---

##### PUT /locations
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/locations](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/locations)
Will update a Location when sent a JSON payload in the format:

```
{
    "locationId": 300003,
    "locationName": "Wimbledon",
    "address1": "26 Lyveden Road",
    "address2": null,
    "city": "London",
    "postCode": "SW17 9DU",
    "country": "UK",
    "longitude": -0.16698,
    "latitude": 51.419942,
    "adminOrg": "Unicef",
    "water": true,
    "drinkable": true,
    "treatment": false,
    "unknown": false,
    "largeWaterFacility": false,
    "maleToilets": true,
    "femaleToilets": true,
    "largeToiletFacility": false,
    "disabledAccess": false,
    "chargeForUse": false,
    "openingHours": "M-F 24hr, Sat 10-21:00, Sun 11-18:00",
    "hasIssue": true,
    "comment": null,
    "pictureURL": "https://oasis-images.s3.eu-west-2.amazonaws.com/location-images/300003.PNG",
    "rating": 5
}
```

##### PUT /users
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/users](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/users)
Will update a User when sent a JSON payload in the format:

```
{
    "email": "joe.blogs@yrtt.com",
    "uuid": "joeblogs_uuid",
    "firstName": "Joe",
    "lastName": "Blogs",
    "countryCode": "44",
    "mobileNumber": "07887345345",
    "adminUser": true,
    "organisation": "Joe's Charity",
    "orgAddress1": "27 Primrose Hill",
    "orgAddress2": "",
    "orgCity": "London",
    "orgPostCode": "NW1 6XL",
    "orgCountry": "UK"
    }
```

##### PUT /facilityProblems
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/facilityProblems](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/facilityProblems)
Will update a FacilityProblem when sent a JSON payload in the format:
It will do this as a transaction - copy the existing record into the FacilityProblemHistory table, then update the existing record in the FacilityProblem table.
This will provide a history of the specific problem.

```
{
    "problemId": 200003,
    "locationId": 300004,
    "version": 2,
    "auditDateTime": 1603502928000,
    "description": "Another PUT test FacilityProblem"
}
```

---

##### DELETE /locations
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/locations](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/locations)
Will delete from the Location table for the given locationId.


##### DELETE /users
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/users](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/users)
Will delete from the User and SavedLocation tables as a transaction for the given user (uuid).


##### DELETE /savedLocations
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/savedLocations](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/savedLocations)
Will delete SavedLocation(s) for the given user (uuid).


##### DELETE /registerNotifications
[https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/registerNotifications](https://j33niy2o35.execute-api.eu-west-2.amazonaws.com/dev/registerNotifications)
Will delete UserRegisteredFacilityNotification for the given uuid and problemId





---
## Instructions

This codebase makes use of Maven for dependencies and build alongside the [serverless framework](./https://serverless.com) for deploying to AWS lambda

As such assumes that Maven, Java and Serverless have all been installed and configured ready to use.

In order to build the application run

```
mvn clean install
```

Then to deploy the Lambda function. 

```
serverless deploy
```

It will provision the required API Gateway (for HTTPS Trigger) and upload the JAR file to Lambda

## Environment Variables 

In order to deploy the application you will need a config file with the following details.

This is referenced in the [serverless.yml](./serverless.yml) and the file is called **config.dev.json**

```
{
  "DB_HOST" : "YOUR_RDS_INSTANCE_ENDPOINT",
  "DB_NAME" : "YOUR_TARGET_DATABASE_NAME",
  "DB_USER" : "A_VALID_MYSQL_USER",
  "DB_PASSWORD" : "VALID_MYSQL_PASSWORD"
}
```