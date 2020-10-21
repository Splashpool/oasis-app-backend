# Oasis App Backend

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