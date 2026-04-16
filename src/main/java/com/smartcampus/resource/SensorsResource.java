package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorsResource {

    private final DataStore db = DataStore.get();

    @GET
    public Collection<Sensor> getSensors(@QueryParam("type") String type) {
        Collection<Sensor> all = db.sensors.values();
        if (type == null || type.isBlank()) {
            return all;
        }
        return all.stream()
                .filter(s -> type.equalsIgnoreCase(s.getType()))
                .collect(Collectors.toList());
    }

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getType() == null || sensor.getType().isBlank()) {
            return Response.status(400)
                    .entity(java.util.Map.of("message", "Sensor type is required"))
                    .build();
        }

        // Validate that the referenced room actually exists
        String roomId = sensor.getRoomId();
        if (roomId == null || !db.rooms.containsKey(roomId)) {
            throw new LinkedResourceNotFoundException(
                    "Room with id '" + roomId + "' does not exist. Cannot register sensor.");
        }

        String id = db.newId();
        String status = sensor.getStatus() != null ? sensor.getStatus() : "ACTIVE";
        Sensor created = new Sensor(id, sensor.getType(), status, 0.0, roomId);

        db.sensors.put(id, created);

        // Link sensor id to the room's sensorIds list
        Room room = db.rooms.get(roomId);
        room.getSensorIds().add(id);

        return Response.status(201).entity(created).build();
    }

    @GET
    @Path("/{sensorId}")
    public Sensor getSensor(@PathParam("sensorId") String sensorId) {
        Sensor s = db.sensors.get(sensorId);
        if (s == null) throw new NotFoundException("Sensor not found: " + sensorId);
        return s;
    }

    // Sub-resource locator — delegates to SensorReadingsResource
    @Path("/{sensorId}/readings")
    public SensorReadingsResource readings(@PathParam("sensorId") String sensorId) {
        return new SensorReadingsResource(sensorId);
    }
}