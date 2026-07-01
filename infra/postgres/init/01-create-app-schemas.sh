#!/bin/sh
set -eu

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  -v db_name="$POSTGRES_DB" \
  -v user_schema="$USER_DB_SCHEMA" \
  -v email_schema="$EMAIL_DB_SCHEMA" \
  -v user_name="$USER_DB_USERNAME" \
  -v user_password="$USER_DB_PASSWORD" \
  -v email_name="$EMAIL_DB_USERNAME" \
  -v email_password="$EMAIL_DB_PASSWORD" <<-'EOSQL'
SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', :'user_name', :'user_password')
WHERE NOT EXISTS (
    SELECT 1 FROM pg_roles WHERE rolname = :'user_name'
)\gexec

SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', :'email_name', :'email_password')
WHERE NOT EXISTS (
    SELECT 1 FROM pg_roles WHERE rolname = :'email_name'
)\gexec

ALTER ROLE :"user_name" WITH LOGIN PASSWORD :'user_password';
ALTER ROLE :"email_name" WITH LOGIN PASSWORD :'email_password';

CREATE SCHEMA IF NOT EXISTS :"user_schema" AUTHORIZATION :"user_name";
CREATE SCHEMA IF NOT EXISTS :"email_schema" AUTHORIZATION :"email_name";

GRANT CONNECT ON DATABASE :"db_name" TO :"user_name";
GRANT CONNECT ON DATABASE :"db_name" TO :"email_name";

GRANT USAGE, CREATE ON SCHEMA :"user_schema" TO :"user_name";
GRANT USAGE, CREATE ON SCHEMA :"email_schema" TO :"email_name";
EOSQL
