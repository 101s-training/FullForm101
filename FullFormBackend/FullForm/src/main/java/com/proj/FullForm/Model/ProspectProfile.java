package com.proj.FullForm.Model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "ProspectProfile")
public class ProspectProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonBackReference
    private ProspectAuth user;

    @Column(nullable = false)
    private String fullName;

    @Column(length = 2000)
    private String bio;

    private String profilePhotoUrl;

    private String introVideoUrl;

    private String audioUrl;

    private String phone;

    private String website;

    private String searchQuery;

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

    private String resumeUrl;

    private String localDataFilePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditionStatus status = AuditionStatus.DRAFT;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
