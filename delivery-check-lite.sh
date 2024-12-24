#!/usr/bin/env bash

mvn clean install -Dmaven.test.skip -DskipTests -T 4
