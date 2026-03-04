# Smart Campus API

REST API for managing rooms, sensors and sensor readings.

## Base URL
http://localhost:8081/api/v1

## How to Run

1. Open project in NetBeans
2. Run Main.java
3. Server starts at:

http://localhost:8081/api/v1

---

## API Endpoints

### Discovery
GET /api/v1

Returns API information and links.

---

### Rooms

GET /api/v1/rooms  
List all rooms

POST /api/v1/rooms
Create a room

Example JSON
{
 "name": "Lab 01",
 "building": "IIT"
}

GET /api/v1/rooms/{roomId}

DELETE /api/v1/rooms/{roomId}

---

### Sensors

POST /api/v1/sensors

GET /api/v1/sensors

GET /api/v1/sensors?type=CO2

---

### Sensor Readings

POST /api/v1/sensors/{sensorId}/readings

GET /api/v1/sensors/{sensorId}/readings

---

## Error Handling

409 - RoomNotEmptyException  
422 - LinkedResourceNotFoundException  
403 - SensorUnavailableException  
500 - GenericErrorMapper

---

## Example CURL

Create room

curl -X POST http://localhost:8081/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"name":"Lab 01","building":"IIT"}'

Get rooms

curl http://localhost:8081/api/v1/rooms