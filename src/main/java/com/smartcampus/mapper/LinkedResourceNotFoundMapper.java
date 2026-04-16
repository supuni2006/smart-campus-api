package com.smartcampus.mapper;

import com.smartcampus.dto.ApiError;
import com.smartcampus.exception.LinkedResourceNotFoundException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        return Response.status(422)
                .entity(new ApiError("LINKED_RESOURCE_NOT_FOUND", ex.getMessage()))
                .build();
    }
}