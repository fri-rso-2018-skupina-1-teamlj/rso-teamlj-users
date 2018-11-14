package si.fri.rso.teamlj.users.api.v1.resources;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;


@RequestScoped
@Path("/info")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InfoResource {

    private Logger log = Logger.getLogger(UsersResource.class.getName());

    @GET
    @Path("info")
    public Response info() {

        JsonObject json = Json.createObjectBuilder()
                .add("clani", Json.createArrayBuilder().add("jp8874@student.uni-lj.si").add("ls.."))
                .add("opis_projekta", "Nas projekt implementira aplikacijo za deljenje koles.")
                .add("mikrostoritve", Json.createArrayBuilder().add("http://35.204.91.158:8081/v1/user"))
                .add("github", Json.createArrayBuilder().add("https://github.com/..."))
                .add("travis", Json.createArrayBuilder().add("https://travis-ci.org/..."))
                .add("dockerhub", Json.createArrayBuilder().add("https://hub.docker.com/r/..."))
                .build();


        return Response.ok(json.toString()).build();
    }

}
