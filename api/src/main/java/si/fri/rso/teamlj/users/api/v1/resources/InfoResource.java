package si.fri.rso.teamlj.users.api.v1.resources;

import com.kumuluz.ee.common.runtime.EeRuntime;

import si.fri.rso.teamlj.users.api.v1.dtos.HealthDto;
import si.fri.rso.teamlj.users.api.v1.dtos.LoadDto;
import si.fri.rso.teamlj.users.services.configuration.AppProperties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/info")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InfoResource {

    private Logger log = Logger.getLogger(InfoResource.class.getName());
    
    @Inject
    private AppProperties appProperties;
     
    @GET
    @Path("instanceid")
    public Response getInstanceId() {
         String instanceId =
                "{\"instanceId\" : \"" + EeRuntime.getInstance().getInstanceId() + "\"}";
         return Response.ok(instanceId).build();
    }
    
    @POST
    @Path("healthy")
    public Response setHealth(HealthDto health) {
        appProperties.setHealthy(health.getHealthy());
        log.info("Setting health to " + health.getHealthy());
        return Response.ok().build();
    }
    
    @POST
    @Path("load")
    public Response loadOrder(LoadDto loadDto) {
         for (int i = 1; i <= loadDto.getN(); i++) {
            fibonacci(i);
        }
         return Response.status(Response.Status.OK).build();
    }
	
    @GET
    @Path("info")
    public Response info() {
		
		//TODO
        JsonObject json = Json.createObjectBuilder()
                .add("clani", Json.createArrayBuilder().add("jp8874@student.uni-lj.si").add("ls8856@student.uni-lj.si"))
                .add("opis_projekta", "Nas projekt implementira aplikacijo za deljenje koles.")
                .add("mikrostoritve", Json.createArrayBuilder().add("http://35.204.91.158:8080/v1/user"))
                .add("github", Json.createArrayBuilder().add("https://github.com/fri-rso-2018-skupina-1-teamlj"))
                .add("travis", Json.createArrayBuilder().add("https://travis-ci.org/..."))
                .add("dockerhub", Json.createArrayBuilder().add("https://hub.docker.com/r/..."))
                .build();


        return Response.ok(json.toString()).build();
    }
    
    private long fibonacci(int n) {
        if (n <= 1) return n;
        else return fibonacci(n - 1) + fibonacci(n - 2);
    }

}
