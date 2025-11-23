#!/bin/bash

echo "╔════════════════════════════════════════════════════════════╗"
echo "║     Fix IntelliJ MapStruct Issues                         ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

services=("auth-service" "user-service" "post-service" "rental-service" "media-service")

for service in "${services[@]}"; do
    if [ -d "$service" ]; then
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "🔧 Processing: $service"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        
        cd "$service"
        
        # Clean and rebuild to generate mappers
        echo "   Cleaning..."
        ./mvnw clean -q
        
        echo "   Generating sources..."
        ./mvnw generate-sources -q
        
        echo "   Compiling..."
        ./mvnw compile -DskipTests -q
        
        # Check if generated sources exist
        if [ -d "target/generated-sources/annotations" ]; then
            mapper_count=$(find target/generated-sources/annotations -name "*Mapper*.java" 2>/dev/null | wc -l)
            if [ $mapper_count -gt 0 ]; then
                echo "   ✅ Generated $mapper_count mapper(s)"
                find target/generated-sources/annotations -name "*Mapper*.java" 2>/dev/null | sed 's/^/      - /'
            else
                echo "   ⚠️  No mappers generated (might not have MapStruct)"
            fi
        else
            echo "   ⚠️  Generated sources directory not found"
        fi
        
        cd ..
        echo ""
    fi
done

echo "╔════════════════════════════════════════════════════════════╗"
echo "║     ✅ DONE! Now in IntelliJ:                              ║"
echo "║     1. Maven → Reload All Maven Projects 🔄                ║"
echo "║     2. Build → Rebuild Project                            ║"
echo "║     3. Run services                                       ║"
echo "╚════════════════════════════════════════════════════════════╝"

