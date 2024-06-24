
# Advance Valuation Ruling Frontend

Service providing the public frontend to ARS.

## Backends

[Advance Valuation Ruling](https://github.com/hmrc/advance-valuation-rulings)

## Persistence
This service uses mongodb to persist user answers.

## Requirements
This service is written in Scala using the Play framework, so needs a JRE to run.

JRE/JDK 11 is recommended.

## Running the service
Use the ARS_ALL profile to run all services using the latest tagged releases

```bash
sm2 --start ARS_ALL
```

### Launching the service locally
To run the service locally on port 12600, use

```bash
sbt run
```

## Testing the service

Run the unit and integration tests locally with the following script. (_includes SCoverage, Scalastyle, Scalafmt_)

```bash
./run_all_tests.sh
```
