package si.fri.rso.teamlj.users.models.dtos;

import java.time.Instant;

public class BikeRent {

    private Integer id;

    private String locationOfRent;

    private String locationOfReturn;

    private Instant dateOfRent;

    private Instant dateOfReturn;

    private String bikeId;

    private String userId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocationOfRent() {
        return locationOfRent;
    }

    public void setLocationOfRent(String locationOfRent) {
        this.locationOfRent = locationOfRent;
    }

    public String getLocationOfReturn() {
        return locationOfReturn;
    }

    public void setLocationOfReturn(String locationOfReturn) {
        this.locationOfReturn = locationOfReturn;
    }

    public Instant getDateOfRent() {
        return dateOfRent;
    }

    public void setDateOfRent(Instant dateOfRent) {
        this.dateOfRent = dateOfRent;
    }

    public Instant getDateOfReturn() {
        return dateOfReturn;
    }

    public void setDateOfReturn(Instant dateOfReturn) {
        this.dateOfReturn = dateOfReturn;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }
}