package com.smartcampus.store;

import com.smartcampus.model.Room;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private static final DataStore INSTANCE = new DataStore();

    public final Map<String, Room> rooms = new ConcurrentHashMap<>();

    private DataStore() {}

    public static DataStore get() {
        return INSTANCE;
    }

    public String newId() {
        return UUID.randomUUID().toString();
    }

    public Object rooms() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}