package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    public final Map<String, Room> rooms = new ConcurrentHashMap<>();
    public final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    public final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {}

    public static DataStore get() {
        return INSTANCE;
    }

    public String newId() {
        return UUID.randomUUID().toString();
    }
}