package si.fri.rso.teamlj.users.models.entities;

import org.eclipse.persistence.annotations.UuidGenerator;
import si.fri.rso.teamlj.users.models.dtos.BikeRent;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity(name = "userTable")
@NamedQueries(value =
        {
                @NamedQuery(name = "User.getAll", query = "SELECT u FROM userTable u")
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String address;

    private boolean inUse;

    @Column(name = "date_of_birth")
    private Instant dateOfBirth;

    @Transient
    private List<BikeRent> rents;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean getInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public Instant getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Instant dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public List<BikeRent> getRents() {
        return rents;
    }

    public void setRents(List<BikeRent> rents) {
        this.rents = rents;
    }
}