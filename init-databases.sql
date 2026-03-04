-- Initialize separate databases for each microservice
-- This script runs automatically when the PostgreSQL container starts for the first time

CREATE DATABASE investtrack_auth;
CREATE DATABASE investtrack_trade;
CREATE DATABASE investtrack_marketdata;
CREATE DATABASE investtrack_analytics;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE investtrack_auth TO postgres;
GRANT ALL PRIVILEGES ON DATABASE investtrack_trade TO postgres;
GRANT ALL PRIVILEGES ON DATABASE investtrack_marketdata TO postgres;
GRANT ALL PRIVILEGES ON DATABASE investtrack_analytics TO postgres;
