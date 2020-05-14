CREATE DATABASE phones_db;
CREATE ROLE scala_app WITH LOGIN PASSWORD 'my_scala_app_psswd';
GRANT ALL PRIVILEGES ON DATABASE phones_db TO scala_app;
