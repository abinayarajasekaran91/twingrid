package com.example.demo.dto;

import java.time.LocalDate;

public class UserProfileResponse {

    private String email;
    private String name;
    private Integer age;
    private LocalDate dob;
    private String city;
    private String address;

    public UserProfileResponse() {
    }

    public UserProfileResponse(String email, String name, Integer age, LocalDate dob, String city, String address) {
        this.email = email;
        this.name = name;
        this.age = age;
        this.dob = dob;
        this.city = city;
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
