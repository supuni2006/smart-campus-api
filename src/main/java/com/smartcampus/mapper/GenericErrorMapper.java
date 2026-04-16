package com.smartcampus.mapper;

import com.smartcampus.dto.ApiError;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericErrorMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable ex) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // 500
                .entity(new ApiError("INTERNAL_ERROR", "An unexpected error occurred."))
                .build();
    }
}