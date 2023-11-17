
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

```bash
sm2 --start ARS_ALL
```

Run `sm2 -s` to check what services are running

### Launching the service locally
To bring up the service on the configured port 12600, use

```bash
sbt run
```

## Testing the service

Run the unit and integration tests locally with the following script. (_includes SCoverage, Scalastyle, Scalafmt_)

```bash
./run_all_tests.sh
```
