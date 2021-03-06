package com.errorsonogsvijeta.treningomat.model.users;

import com.errorsonogsvijeta.treningomat.model.geo.City;
import com.errorsonogsvijeta.treningomat.model.training.Sport;
import com.errorsonogsvijeta.treningomat.model.training.TrainingGroup;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
public class Trainer extends User {
    private String name;
    private String surname;
    private List<Sport> sports;
    private Long PID;
    private String phoneNumber;
    private String idPhoto;
    private City city;
    private String address;
    private List<TrainingGroup> trainingGroups;
    private MultipartFile file;

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(nullable = false)
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @ManyToMany
    public List<Sport> getSports() {
        return sports;
    }

    public void setSports(List<Sport> sports) {
        this.sports = sports;
    }

    @Column(nullable = false)
    public Long getPID() {
        return PID;
    }

    public void setPID(Long PID) {
        this.PID = PID;
    }

    @Column
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Column
    public String getIdPhoto() {
        return idPhoto;
    }

    public void setIdPhoto(String idPhoto) {
        this.idPhoto = idPhoto;
    }

    @ManyToOne
    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    @Column
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @OneToMany(mappedBy = "trainer")
    public List<TrainingGroup> getTrainingGroups() {
        return trainingGroups;
    }

    public void setTrainingGroups(List<TrainingGroup> trainingGroups) {
        this.trainingGroups = trainingGroups;
    }

    @Transient
    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname, PID);
    }

    public String fullName() {
        return String.format("%s %s", name, surname);
    }

    @Override
    public String toString() {
        return fullName();
    }
}
