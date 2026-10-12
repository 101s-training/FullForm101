package com.proj.FullForm.DTO;

import com.proj.FullForm.Model.AuditionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProspectResponse {
    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private String website;
    private String search;
    private String bio;
    private Integer age;
    private String favouriteColor;
    private String hatedColor;
    private Integer skillLevel;
    private String dob;
    private String slot;
    private String graduationMonth;
    private String week;
    private String meetingTime;
    private String country;
    private String languages;
    private String framework;
    private String gender;
    private String interests;
    private String profilePhotoUrl;
    private String introVideoUrl;
    private String audioUrl;
    private String resumeUrl;
    private String localDataFilePath;
    private String localTextContent;
    private AuditionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
