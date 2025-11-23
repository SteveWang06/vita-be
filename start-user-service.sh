#!/bin/bash

echo "🚀 Starting User Service via Maven..."
echo ""

cd user-service

# Kill any existing process on port 8081
lsof -ti:8081 | xargs kill -9 2>/dev/null || echo "Port 8081 is free"

# Start service
./mvnw spring-boot:run

