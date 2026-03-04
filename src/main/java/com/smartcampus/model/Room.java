package com.smartcampus.model;

public class Room {
    public String id;
    public String name;
    public String building;

    public Room() {}

    public Room(String id, String name, String building) {
        this.id = id;
        this.name = name;
        this.building = building;
    }
}