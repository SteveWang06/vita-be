#!/bin/bash

echo "🚀 Running User Service from JAR..."
echo ""

cd user-service

# Kill existing process
echo "Checking for existing process on port 8081..."
lsof -ti:8081 | xargs kill -9 2>/dev/null && echo "✅ Killed existing process" || echo "✅ Port 8081 is free"

echo ""
echo "Starting User Service..."
echo ""

# Run from JAR
java -jar target/user-service-0.0.1-SNAPSHOT.jar

