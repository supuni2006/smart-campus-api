package com.smartcampus.mapper;

import com.smartcampus.dto.ApiError;
import com.smartcampus.exception.RoomNotEmptyException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        return Response.status(Response.Status.CONFLICT) // 409
                .entity(new ApiError("ROOM_NOT_EMPTY", ex.getMessage()))
                .build();
    }
}