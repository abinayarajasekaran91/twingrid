package com.example.demo.dto;

import java.time.LocalDate;

public class PanResponse {

    private String pan;
    private String name;
    private String email;
    private Integer age;
    private LocalDate dob;
    private String city;
    private String address;

    public PanResponse() {
    }

    public PanResponse(String pan, String name, String email, Integer age, LocalDate dob, String city, String address) {
        this.pan = pan;
        this.name = name;
        this.email = email;
        this.age = age;
        this.dob = dob;
        this.city = city;
        this.address = address;
    }

    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
