package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover(@Context UriInfo uriInfo) {
        String base = uriInfo.getBaseUri().toString();

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", "Smart Campus API");
        info.put("version", "1.0");
        info.put("description", "RESTful API for managing campus rooms and IoT sensors");
        info.put("contact", "admin@smartcampus.ac.uk");
        info.put("status", "running");

        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms", base + "rooms");
        links.put("sensors", base + "sensors");
        info.put("resources", links);

        return Response.ok(info).build();
    }
}
