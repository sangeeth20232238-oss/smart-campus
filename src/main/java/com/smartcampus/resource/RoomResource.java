package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(store.getRooms().values());
        return Response.ok(rooms).build();
    }

    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null) {
            return Response.status(400).entity("Room id is required").build();
        }
        store.getRooms().put(room.getId(), room);
        return Response.status(201).entity(room).build();
    }
}
