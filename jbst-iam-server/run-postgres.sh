#!/usr/bin/env bash
# shellcheck disable=SC2046

# print/colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NO_COLOR='\033[0m'

print() { echo -e "${1}${2}${NO_COLOR}"; }

# java

METHOD=maven
PORT=3002
SPRING_BOOT_PROFILE=postgres
SPRING_BOOT_CONFIG_LOCATION=classpath:application.yml,classpath:application-dev.yml,classpath:application-postgres.yml,classpath:application-postgres-dev.yml
JASYPT_PASSWORD=JJJJBSTGH
JVM_ARGUMENTS="-Xms512m -Xmx2g --add-opens=java.base/java.time=ALL-UNNAMED --add-opens=java.base/java.math=ALL-UNNAMED"

print "================================================================================================================="
print "${BLUE}" "PostgreSQL 'jbst': STARTED"
print "================================================================================================================="

docker run --rm --network jbst-network jbergknoff/postgresql-client postgresql://postgres:postgres@jbst-postgres:5432/postgres -c "CREATE DATABASE jbst"

print "================================================================================================================="
print "${GREEN}" "PostgreSQL 'jbst': COMPLETED"
print "================================================================================================================="

java-run-spring-boot-dev-profile-v4.sh $METHOD $PORT "$SPRING_BOOT_PROFILE" $SPRING_BOOT_CONFIG_LOCATION $JASYPT_PASSWORD "$JVM_ARGUMENTS"
