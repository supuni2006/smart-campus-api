package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;

public class Main {

    public static final String BASE_URI = "http://localhost:8081/";

    public static HttpServer startServer() {
        return GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI),
                new SmartCampusApplication()
        );
    }

    public static void main(String[] args) throws IOException {

        HttpServer server = startServer();

        System.out.println("Smart Campus API running at:");
        System.out.println("http://localhost:8081/api/v1");

        System.in.read();

        server.shutdownNow();
    }
}