#!/bin/bash
echo "Installing PostgreSQL..."
brew install postgresql@14

echo "Starting PostgreSQL service..."
brew services start postgresql@14

echo "Waiting for PostgreSQL to start..."
sleep 5

echo "Setting up postgres user and database..."
export PATH="/opt/homebrew/opt/postgresql@14/bin:/usr/local/opt/postgresql@14/bin:$PATH"
createuser -s postgres || echo "User might already exist"
createdb -U postgres postgres || echo "Database might already exist"
psql -U postgres -d postgres -c "ALTER USER postgres PASSWORD 'postgres';"

echo "PostgreSQL setup complete!"
