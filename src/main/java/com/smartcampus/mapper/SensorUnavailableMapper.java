package com.smartcampus.mapper;

import com.smartcampus.dto.ApiError;
import com.smartcampus.exception.SensorUnavailableException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException ex) {
        return Response.status(Response.Status.FORBIDDEN) // 403
                .entity(new ApiError("SENSOR_UNAVAILABLE", ex.getMessage()))
                .build();
    }
}