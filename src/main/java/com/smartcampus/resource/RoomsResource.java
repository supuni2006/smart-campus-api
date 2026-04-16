package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
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
        if (room == null || room.getName() == null || room.getName().isBlank()) {
            return Response.status(400)
                    .entity(java.util.Map.of("message", "Room name is required"))
                    .build();
        }

        String id = db.newId();
        Room created = new Room(id, room.getName(), room.getCapacity());
        db.rooms.put(id, created);

        return Response.status(201).entity(created).build();
    }

    @GET
    @Path("/{roomId}")
    public Room getRoom(@PathParam("roomId") String roomId) {
        Room r = db.rooms.get(roomId);
        if (r == null) throw new NotFoundException("Room not found: " + roomId);
        return r;
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room r = db.rooms.get(roomId);
        if (r == null) throw new NotFoundException("Room not found: " + roomId);

        // Business rule: cannot delete a room that still has sensors assigned
        if (!r.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room '" + roomId + "' cannot be deleted because it still has " +
                    r.getSensorIds().size() + " sensor(s) assigned to it.");
        }

        db.rooms.remove(roomId);
        return Response.noContent().build(); // 204
    }
}
