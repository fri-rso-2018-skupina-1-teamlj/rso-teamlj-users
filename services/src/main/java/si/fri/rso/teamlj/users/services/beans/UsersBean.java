package si.fri.rso.teamlj.users.services.beans;


import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;

import org.eclipse.microprofile.metrics.annotation.Timed;

import si.fri.rso.teamlj.users.models.dtos.BikeRent;
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
import javax.ws.rs.core.UriInfo;
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

	@Timed
    public List<User> getUsers() {

        TypedQuery<User> query = em.createNamedQuery("User.getAll", User.class);

        return query.getResultList();

    }

    public List<User> getUsersFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0)
                .build();

        return JPAUtils.queryEntities(em, User.class, queryParameters);
    }

    public User getUser(Integer userId) {

        User user = em.find(User.class, userId);

        if (user == null) {
            log.warning("user do not exist/user was deleted");
            throw new NotFoundException();
        }

        List<BikeRent> rents = usersBean.getRents(userId);
        user.setRents(rents);

        return user;
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
                    //.target("http://localhost:8081/v1/rents?where=userId:EQ:" + userId)
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
                        //.target("http://localhost:8081/v1/rents/rentabike/" + userId + "/" + bikeId)
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

    public User returnBike(Integer userId, Integer rentId)
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
                    .target(baseUrl.get()  + "/v1/rents/returnabike/" + userId + "/" + rentId)
                    //.target("http://localhost:8081/v1/rents/returnabike/" + userId + "/" + rentId)
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
                    //.target("http://localhost:8083/v1/payments/pay/" + userId)
                    .request()
                    .build("POST", Entity.json(""))
                    .invoke();
        } catch (WebApplicationException | ProcessingException e) {
            log.severe(e.getMessage());
            throw new InternalServerErrorException(e);
        }

        return user;
    }

    public User subscribed(Integer userId) {

        User user = em.find(User.class, userId);

        if (user == null) {
            log.warning("user do not exist");
            throw new NotFoundException();
        }

        // TODO - check, ali je tole ok? mislim da ni

        try {
            httpClient
                    .target(baseUrlPay.get()  + "/v1/payments/subscribed/" + userId)
//                    .target("http://localhost:8083/v1/payments/subscribed/" + userId)
                    .request()
                    .build("PUT", Entity.json(""))
                    .invoke();
        } catch (WebApplicationException | ProcessingException e) {
            log.severe(e.getMessage());
            throw new InternalServerErrorException(e);
        }

        return user;
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
