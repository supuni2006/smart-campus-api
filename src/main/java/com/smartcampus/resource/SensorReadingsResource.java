package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingsResource {

    private final String sensorId;
    private final DataStore db = DataStore.get();

    public SensorReadingsResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public List<SensorReading> getReadings() {
        return db.readings.getOrDefault(sensorId, List.of());
    }

    public static class CreateReadingRequest {
        public double value;
    }

    @POST
    public Response addReading(CreateReadingRequest request) {
        Sensor sensor = db.sensors.get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }

        // Part 5.3 - Block readings for sensors under maintenance
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently under MAINTENANCE and cannot accept new readings.");
        }

        String id = db.newId();
        SensorReading reading = new SensorReading(id, request.value);

        db.readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);

        // Part 4.2 side-effect: update currentValue on the parent sensor
        sensor.setCurrentValue(request.value);

        return Response.status(201).entity(reading).build();
    }
}
