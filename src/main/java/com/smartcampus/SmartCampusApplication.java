package com.smartcampus;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

import com.smartcampus.resource.*;
import com.smartcampus.mapper.*;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        register(RootResource.class);
        register(RoomsResource.class);
        register(SensorsResource.class);

        register(RoomNotEmptyMapper.class);
        register(LinkedResourceNotFoundMapper.class);
        register(SensorUnavailableMapper.class);
        register(GenericErrorMapper.class);

        packages("org.glassfish.jersey.jackson");
    }
}
