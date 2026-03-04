package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/sensors")
public class SensorsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getSensors() {

        return List.of(
                "Temperature Sensor",
                "CO2 Sensor",
                "Humidity Sensor"
        );
    }
}