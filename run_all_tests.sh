#!/usr/bin/env bash
sbt clean scalafmtAll scalastyleAll compile coverage Test/test it/test coverageOff coverageReport dependencyUpdates
