#!/usr/bin/env bash
sbt clean scalafmtAll scalastyleAll compile coverage test it/test coverageOff coverageReport dependencyUpdates
