#!/bin/bash

echo "╔════════════════════════════════════════════════════════════╗"
echo "║     Building All Vita Backend Services                    ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Array of services
services=("auth-service" "user-service" "post-service" "rental-service" "media-service" "api-gateway")

# Build each service
for service in "${services[@]}"; do
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📦 Building: $service"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    if [ -d "$service" ]; then
        cd "$service"
        
        # Build
        ./mvnw clean compile -DskipTests
        
        if [ $? -eq 0 ]; then
            echo "✅ $service: BUILD SUCCESS"
        else
            echo "❌ $service: BUILD FAILED"
            exit 1
        fi
        
        cd ..
        echo ""
    else
        echo "⚠️  Directory not found: $service"
        echo ""
    fi
done

echo "╔════════════════════════════════════════════════════════════╗"
echo "║     ✅ ALL SERVICES BUILT SUCCESSFULLY!                    ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "🚀 You can now start all services from IntelliJ or run:"
echo ""
for service in "${services[@]}"; do
    if [ -d "$service" ]; then
        echo "   cd $service && ./mvnw spring-boot:run"
    fi
done
echo ""

