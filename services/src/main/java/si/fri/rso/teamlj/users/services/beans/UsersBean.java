package si.fri.rso.teamlj.users.services.beans;


import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.metrics.annotation.Counted;

import si.fri.rso.teamlj.users.models.dtos.BikeRent;
import si.fri.rso.teamlj.users.models.dtos.Payment;
import si.fri.rso.teamlj.users.models.entities.User;
import si.fri.rso.teamlj.users.services.configuration.AppProperties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


@RequestScoped
public class UsersBean {

    private Logger log = Logger.getLogger(UsersBean.class.getName());

    @Inject
    private EntityManager em;

    @Inject
    private AppProperties appProperties;

    @Inject
    private UsersBean usersBean;

    private Client httpClient;

    @Inject
    @DiscoverService("rso-rents")
    private Optional<String> baseUrl;

    @Inject
    @DiscoverService("rso-payments")
    private Optional<String> baseUrlPay;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
        //baseUrl = "http://localhost:8081"; // only for demonstration
    }

	@Timed(name = "get_users_timed")
    @Counted(name = "get_users_counter")
    @CircuitBreaker(requestVolumeThreshold = 3)
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "getUsersFallback")
    public List<User> getUsers() {

        TypedQuery<User> query = em.createNamedQuery("User.getAll", User.class);

        return query.getResultList();

    }

    public List<User> getUsersFallback() {

        return Collections.emptyList();

    }

    @Timed(name = "get_users_filtered_timed")
    @Counted(name = "get_users_filtered_counter")
    public List<User> getUsersFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0)
                .build();

        return JPAUtils.queryEntities(em, User.class, queryParameters);
    }

    @Timed(name = "get_user_timed")
	@Counted(name = "get_user_counter")
    @CircuitBreaker(requestVolumeThreshold = 3)
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "getUserFallback")
    public User getUser(Integer userId) {

        User user = em.find(User.class, userId);

        if (user == null) {
            log.warning("user do not exist/user was deleted - method getUser");
            //throw new NotFoundException();
        }

        List<BikeRent> rents = usersBean.getRents(userId);
        user.setRents(rents);

        return user;
    }

    @CircuitBreaker(requestVolumeThreshold = 3)
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "getUserFallbackEmpty")
    public User getUserFallback(Integer userId) {

        User user = em.find(User.class, userId);

        if (user == null) {
            log.warning("user do not exist/user was deleted - method getUserFallback");
            //throw new NotFoundException();
        }

        user.setRents(Collections.emptyList());

        return user;

    }

    public User getUserFallbackEmpty(Integer userId) {

        log.warning("user do not exist/user was deleted - method getUserFallbackEmpty");

        return new User();

    }

    public User createUser(User user) {

        try {
            beginTx();
            em.persist(user);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return user;
    }

    public User putUser(Integer userId, User user) {

        User u = em.find(User.class, userId);

        if (u == null) {
            return null;
        }

        try {
            beginTx();
            user.setId(u.getId());
            user = em.merge(user);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return user;
    }

    public boolean deleteUser(Integer userId) {

        User user = em.find(User.class, userId);

        if (user != null) {
            try {
                beginTx();
                em.remove(user);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else
            return false;

        return true;
    }

    public List<BikeRent> getRents(Integer userId) {

        try {
            return httpClient
                    .target(baseUrl.get()  + "/v1/rents?where=userId:EQ:" + userId)
//                    .target("http://localhost:8081/v1/rents?where=userId:EQ:" + userId)
                    .request().get(new GenericType<List<BikeRent>>() {
                    });
        } catch (WebApplicationException | ProcessingException e) {
            log.severe(e.getMessage());
            throw new InternalServerErrorException(e);
        }

    }

    public User rent(Integer userId, Integer bikeId)
    {
        /** TODO :
         * - (preveri, če user exist) --- no
         * - (preveri, če bike exist) --- no
         * - preveri, da uporabnik, še nima izposojenega kolesa --- jes
         * - PREVERI, ČE IMA USER NAROČNINO -- PAYMENT
         */

        User u = em.find(User.class, userId);

        if (u == null) {
            return null;
        }

        if(!u.getInUse()) {

            try {
                beginTx();
                u.setInUse(true);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }

            try {
                httpClient
                        .target(baseUrl.get()  + "/v1/rents/rentabike/" + userId + "/" + bikeId)
//                        .target("http://localhost:8081/v1/rents/rentabike/" + userId + "/" + bikeId)
                        .request()
                        .build("POST", Entity.json(""))
                        .invoke();
            } catch (WebApplicationException | ProcessingException e) {
                log.severe(e.getMessage());
                throw new InternalServerErrorException(e);
            }
        }

        return u;
    }

    public User returnBike(Integer userId, Integer rentId, Integer mapId)
    {
        User u = em.find(User.class, userId);

        if (u == null) {
            return null;
        }

        try {
            beginTx();
            u.setInUse(false);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        try {
            httpClient
                    .target(baseUrl.get()  + "/v1/rents/returnabike/" + userId + "/" + rentId + "/" + mapId)
//                    .target("http://localhost:8081/v1/rents/returnabike/" + userId + "/" + rentId + "/" + mapId)
                    .request()
                    .build("PUT", Entity.json(""))
                    .invoke();
        } catch (WebApplicationException | ProcessingException e) {
            log.severe(e.getMessage());
            throw new InternalServerErrorException(e);
        }

        return u;
    }

    public User pay(Integer userId) {

        User user = em.find(User.class, userId);

        if (user == null) {
            log.warning("user do not exist");
            throw new NotFoundException();
        }

        try {
            httpClient
                    .target(baseUrlPay.get()  + "/v1/payments/pay/" + userId)
//                    .target("http://localhost:8083/v1/payments/pay/" + userId)
                    .request()
                    .build("POST", Entity.json(""))
                    .invoke();
        } catch (WebApplicationException | ProcessingException e) {
            log.severe(e.getMessage());
            throw new InternalServerErrorException(e);
        }

        return user;
    }

    public String subscribed(Integer userId) {

        User user = em.find(User.class, userId);

        if (user == null) {
            log.warning("user do not exist");
            throw new NotFoundException();
        }


        try {
            Response response =  httpClient
                    .target(baseUrlPay.get()  + "/v1/payments/subscribed/" + userId)
//                    .target("http://localhost:8083/v1/payments/subscribed/" + userId)
                    .request()
                    .build("PUT", Entity.json(""))
                    .invoke();

            return response.readEntity(String.class);
        } catch (WebApplicationException | ProcessingException e) {
            log.severe(e.getMessage());
            throw new InternalServerErrorException(e);
        }


    }

    private void beginTx() {
        if (!em.getTransaction().isActive())
            em.getTransaction().begin();
    }

    private void commitTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().commit();
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().rollback();
    }
}
