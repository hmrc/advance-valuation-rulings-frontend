#!/usr/bin/env bash
sbt clean scalafmtAll scalastyleAll compile coverage Test/test IntegrationTest/test coverageOff coverageReport dependencyUpdates
