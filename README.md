
# Advance Valuation Ruling Frontend

Service providing the public frontend to ARS.

## Backends

[Advance Valuation Ruling](https://github.com/hmrc/advance-valuation-rulings)

## Persistence
This service uses mongodb to persist user answers.

## Requirements
This service is written in Scala using the Play framework, so needs at least a JRE to run.

JRE/JDK 11 is recommended.

The service also depends on mongodb.

## Running the service
Using service manager (sm or sm2)
Use the ARS_ALL profile to bring up all services using the latest tagged releases
``sm --start ARS_ALL``

run `sm -s` to check what services are running
###Launching the service locally
To bring up the service on the configured port 12600, use
``sbt run``
## Testing the service
This service uses sbt-scoverage to provide test coverage reports.

Use the following command to run the tests with coverage and generate a report.
`sbt clean coverage test it:test coverageReport`
