#!/bin/bash

BASE_URL="http://localhost:8080/api/v1"
HEADER="Content-Type: application/json"

echo "✅ Registering SENSOR_010..."
curl -i -u user:password -X POST "$BASE_URL/sensors" \
  -H "$HEADER" \
  -d '{"tag":"SENSOR_010", "location":"Berlin", "timeZone":"Europe/Berlin"}'

echo -e "\n\n❌ Registering SENSOR_010 again (should fail with 409)..."
curl -i -u user:password admin:password -X POST "$BASE_URL/sensors" \
  -H "$HEADER" \
  -d '{"tag":"SENSOR_010", "location":"Berlin", "timeZone":"Europe/Berlin"}'

echo -e "\n\n📋 Listing all sensors..."
curl -i -u user:password -X GET "$BASE_URL/sensors"

echo -e "\n\n📍 Getting sensors in Berlin..."
curl -i -u user:password -X GET "$BASE_URL/sensors/location/Berlin"

echo -e "\n\n❌ Sending malformed JSON (missing comma)..."
curl -i -u user:password -X POST "$BASE_URL/sensors" \
  -H "$HEADER" \
  -d '{"tag":"SENSOR_999" "location":"Nowhere", "timeZone":"Europe/Nowhere"}'



# Dates for testing (ISO 8601 with timezone)
START_TIME="2025-07-05T10:00:00Z"
END_TIME="2025-07-06T10:00:00Z"
SENSOR_TAG="SENSOR_010"
LOCATION="Berlin"
TIMEZONE="Europe/Berlin"

echo -e "\n\n✅ Register Reading SENSOR_010..."
curl -i -X POST "$BASE_URL/sensor-readings" \
  -H "Content-Type: application/json" \
  -d '{
    "sensorId": "'"$SENSOR_TAG"'",
    "temperature": 23.5,
    "humidity": 60.0,
    "windSpeed": 12.3,
    "timestamp": "2025-07-05T10:10:00Z"
  }'

echo -e "\n\n✅ Scan all readings ..."
curl -i -G "$BASE_URL/sensor-readings" \
  --data-urlencode "startTime=$START_TIME" \
  --data-urlencode "endTime=$END_TIME"

echo-e  "\n\n✅ Query average ..."
curl -i -G "$BASE_URL/sensor-readings/average" \
  --data-urlencode "startTime=$START_TIME" \
  --data-urlencode "endTime=$END_TIME"

echo -e "\n\n✅ Query average for SENSOR_010..."
curl -i -G "$BASE_URL/sensor-readings/average/$SENSOR_TAG" \
  --data-urlencode "startTime=$START_TIME" \
  --data-urlencode "endTime=$END_TIME"