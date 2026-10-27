package io.quarkiverse.logbook.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/logbook")
@ApplicationScoped
public class LogbookResource {

    @GET
    public String hello() {
        return "Hello logbook";
    }
}
