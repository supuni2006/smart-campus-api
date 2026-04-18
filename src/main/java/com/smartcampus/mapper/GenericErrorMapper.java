package com.smartcampus.mapper;

import com.smartcampus.dto.ApiError;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericErrorMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable ex) {
        // Let JAX-RS WebApplicationExceptions (404, 415, etc.) pass through normally
        if (ex instanceof WebApplicationException wae) {
            return wae.getResponse();
        }
        // Catch all unexpected runtime errors and return a safe 500
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiError("INTERNAL_ERROR", "An unexpected error occurred."))
                .build();
    }
}