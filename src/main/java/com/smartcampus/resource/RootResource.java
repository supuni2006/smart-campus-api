package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@Path("/")
public class RootResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> discovery() {

        return Map.of(
                "name", "Smart Campus API",
                "version", "v1",
                "links", Map.of(
                        "rooms", "/api/v1/rooms",
                        "sensors", "/api/v1/sensors"
                )
        );
    }
}