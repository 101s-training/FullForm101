package com.proj.FullForm.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProspectRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String website;
    private String search;
    private String bio;
    private Integer age;
    private String color;
    private String hatedColor;
    private Integer skill;
    private String dob;
    private String slot;
    private String month;
    private String week;
    private String meeting;
    private String country;
    private List<String> languages;
    private String framework;
    private String gender;
    private List<String> interests;
}
