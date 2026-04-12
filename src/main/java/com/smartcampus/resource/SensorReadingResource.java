package com.smartcampus.resource;

import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        List<SensorReading> list = store.getReadingsForSensor(sensorId);
        return Response.ok(list).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        if (reading == null) {
            return Response.status(400).entity("Reading body required").build();
        }
        reading.setId(UUID.randomUUID().toString());
        reading.setTimestamp(System.currentTimeMillis());
        store.addReading(sensorId, reading);
        // Fix: update parent sensor currentValue for data consistency
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }
        return Response.status(201).entity(reading).build();
    }
}
