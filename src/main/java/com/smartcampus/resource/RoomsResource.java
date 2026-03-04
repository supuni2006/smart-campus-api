package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collection;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomsResource {

    private final DataStore db = DataStore.get();

    @GET
    public Collection<Room> listRooms() {
        return db.rooms.values();
    }

    @POST
    public Response createRoom(Room room) {
        if (room == null || room.name == null || room.name.isBlank()) {
            return Response.status(400)
                    .entity(java.util.Map.of("message", "Room name is required"))
                    .build();
        }

        String id = db.newId();
        Room created = new Room(id, room.name, room.building);

        db.rooms.put(id, created);

        return Response.status(201).entity(created).build();
    }

    @GET
    @Path("/{roomId}")
    public Room getRoom(@PathParam("roomId") String roomId) {
        Room r = db.rooms.get(roomId);
        if (r == null) throw new NotFoundException("Room not found");
        return r;
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room r = db.rooms.get(roomId);
        if (r == null) throw new NotFoundException("Room not found");

        db.rooms.remove(roomId);
        return Response.noContent().build(); // 204
    }
}