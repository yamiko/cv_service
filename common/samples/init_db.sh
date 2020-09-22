#!/bin/bash
set -e


# global variables
#
DB_USER='test'
DB_NAME='test_db'


# create the user, the database and set up the access
#
echo "Creating database: $DB_NAME and user: $DB_USER"
psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" <<-EOSQL
CREATE DATABASE $DB_NAME;
CREATE ROLE $DB_USER WITH PASSWORD 'test' LOGIN;
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO test;
EOSQL


# cleanup
#
echo "Done with initializing the sample data."

