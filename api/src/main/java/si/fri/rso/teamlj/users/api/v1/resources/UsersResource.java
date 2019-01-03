package si.fri.rso.teamlj.users.api.v1.resources;

import com.kumuluz.ee.logs.cdi.Log;
import si.fri.rso.teamlj.users.models.dtos.Payment;
import si.fri.rso.teamlj.users.models.entities.User;
import si.fri.rso.teamlj.users.services.beans.UsersBean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/users")
@Log
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsersResource {

    private Logger log = Logger.getLogger(UsersResource.class.getName());

    @Inject
    private UsersBean usersBean;

    @Context
    protected UriInfo uriInfo;

    @GET
    public Response getUsers() {

        List<User> users = usersBean.getUsers();

        return Response.ok(users).build();
    }

    @GET
    @Path("/filtered")
    public Response getUsersFiltered() {

        List<User> users;

        users = usersBean.getUsersFilter(uriInfo);

        return Response.status(Response.Status.OK).entity(users).build();
    }

    @GET
    @Path("/{userId}")
    public Response getUser(@PathParam("userId") Integer userId) {

        User user = usersBean.getUser(userId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(user).build();
    }

    @POST
    public Response createUser(User user) {

        if ((user.getFirstName() == null || user.getFirstName().isEmpty()) || (user.getLastName() == null
                || user.getLastName().isEmpty())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            user = usersBean.createUser(user);
        }

        if (user.getId() != null) {
            return Response.status(Response.Status.CREATED).entity(user).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(user).build();
        }
    }

    @PUT
    @Path("/{userId}")
    public Response putUser(@PathParam("userId") Integer userId, User user) {

        user = usersBean.putUser(userId, user);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (user.getId() != null)
                return Response.status(Response.Status.OK).entity(user).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @DELETE
    @Path("/{userId}")
    public Response deleteUser(@PathParam("userId") Integer userId) {

        boolean deleted = usersBean.deleteUser(userId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/{userId}/rent/{bikeId}")
    public Response rent(@PathParam("userId") Integer userId, @PathParam("bikeId") Integer bikeId)
    {
        User user = usersBean.rent(userId, bikeId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (user.getId() != null)
                return Response.status(Response.Status.OK).entity(user).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @PUT
    @Path("/{userId}/return/{rentId}/{mapId}")
    public Response returnBike(@PathParam("userId") Integer userId, @PathParam("rentId") Integer rentId, @PathParam("mapId") Integer mapId)
    {
        User user = usersBean.returnBike(userId, rentId, mapId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (user.getId() != null)
                return Response.status(Response.Status.OK).entity(user).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @GET
    @Path("/pay/{userId}")
    public Response pay(@PathParam("userId") Integer userId) {

        User user = usersBean.pay(userId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(user).build();
    }

    @GET
    @Path("/subscribed/{userId}")
    public Response subscribed(@PathParam("userId") Integer userId) {

        String result = usersBean.subscribed(userId);

        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (result.equals(""))
                return Response.status(Response.Status.OK).entity("Ni veljavne naročnine").build();
            else
                return Response.status(Response.Status.OK).entity(result).build();
        }
    }

}
