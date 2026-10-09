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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditionStatus status = AuditionStatus.DRAFT;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
