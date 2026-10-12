package com.proj.FullForm.Service;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import com.proj.FullForm.DTO.ProspectResponse;
import com.proj.FullForm.Model.AuditionStatus;
import com.proj.FullForm.Model.ProspectAuth;
import com.proj.FullForm.Model.ProspectProfile;
import com.proj.FullForm.Model.ProspectRole;
import com.proj.FullForm.Repository.ProspectAuthRepository;
import com.proj.FullForm.Repository.ProspectProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml", "image/bmp", "image/*"
    );

    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/webm", "video/quicktime", "video/x-matroska", "video/avi", "video/mpeg", "video/*"
    );

    private static final Set<String> ALLOWED_DOC_TYPES = Set.of(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain", "application/*"
    );

    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of(
            "audio/mpeg", "audio/wav", "audio/mp4", "audio/ogg", "audio/*"
    );

    private final ProspectAuthRepository authRepository;
    private final ProspectProfileRepository profileRepository;
    private final FileStorageService storageService;
    private final ObjectMapper objectMapper;

    public SubmissionService(ProspectAuthRepository authRepository,
                             ProspectProfileRepository profileRepository,
                             FileStorageService storageService) {
        this.authRepository = authRepository;
        this.profileRepository = profileRepository;
        this.storageService = storageService;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public ProspectResponse saveSubmission(
            String name,
            String email,
            String password,
            String phone,
            String website,
            String search,
            String bio,
            Integer age,
            String color,
            String hatedColor,
            Integer skill,
            String dob,
            String slot,
            String month,
            String week,
            String meeting,
            String country,
            List<String> languages,
            String framework,
            String gender,
            List<String> interests,
            MultipartFile photo,
            MultipartFile video,
            MultipartFile resume,
            MultipartFile audio
    ) throws IOException {

        String safeEmail = (email != null && !email.isBlank()) ? email.trim().toLowerCase() : "user_" + UUID.randomUUID() + "@example.com";
        String safePassword = (password != null && !password.isBlank()) ? password : "DefaultPassword@123";

        // 1. Find or create ProspectAuth user
        ProspectAuth user = authRepository.findByEmail(safeEmail)
                .orElseGet(() -> {
                    ProspectAuth newUser = new ProspectAuth();
                    newUser.setEmail(safeEmail);
                    newUser.setPassword(safePassword);
                    newUser.setRole(ProspectRole.USER);
                    return authRepository.save(newUser);
                });

        // 2. Find or create ProspectProfile
        ProspectProfile profile = profileRepository.findByUser(user)
                .orElseGet(() -> {
                    ProspectProfile newProfile = new ProspectProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        // 3. Set text attributes
        profile.setFullName((name != null && !name.isBlank()) ? name.trim() : "Anonymous User");
        profile.setBio(bio);
        profile.setPhone(phone);
        profile.setWebsite(website);
        profile.setSearchQuery(search);
        profile.setAge(age);
        profile.setFavouriteColor(color);
        profile.setHatedColor(hatedColor);
        profile.setSkillLevel(skill != null ? skill : 5);
        profile.setDob(dob);
        profile.setSlot(slot);
        profile.setGraduationMonth(month);
        profile.setWeek(week);
        profile.setMeetingTime(meeting);
        profile.setCountry(country);
        profile.setLanguages(languages != null ? String.join(", ", languages) : null);
        profile.setFramework(framework);
        profile.setGender(gender);
        profile.setInterests(interests != null ? String.join(", ", interests) : null);
        profile.setStatus(AuditionStatus.SUBMITTED);

        // 4. Store uploaded files locally in the file system
        if (photo != null && !photo.isEmpty()) {
            storageService.delete(profile.getProfilePhotoUrl());
            String storedPath = storageService.store(photo, "images", ALLOWED_IMAGE_TYPES);
            profile.setProfilePhotoUrl("/uploads/" + storedPath);
        }

        if (video != null && !video.isEmpty()) {
            storageService.delete(profile.getIntroVideoUrl());
            String storedPath = storageService.store(video, "videos", ALLOWED_VIDEO_TYPES);
            profile.setIntroVideoUrl("/uploads/" + storedPath);
        }

        if (resume != null && !resume.isEmpty()) {
            storageService.delete(profile.getResumeUrl());
            String storedPath = storageService.store(resume, "documents", ALLOWED_DOC_TYPES);
            profile.setResumeUrl("/uploads/" + storedPath);
        }

        if (audio != null && !audio.isEmpty()) {
            storageService.delete(profile.getAudioUrl());
            String storedPath = storageService.store(audio, "audio", ALLOWED_AUDIO_TYPES);
            profile.setAudioUrl("/uploads/" + storedPath);
        }

        // 5. Persist profile to database first to ensure ID is generated
        profile = profileRepository.save(profile);

        // 6. Save text and metadata to a local JSON file in uploads/submissions/
        Map<String, Object> localRecord = new LinkedHashMap<>();
        localRecord.put("submissionId", profile.getId());
        localRecord.put("userId", user.getId());
        localRecord.put("email", user.getEmail());
        localRecord.put("fullName", profile.getFullName());
        localRecord.put("bio", profile.getBio());
        localRecord.put("phone", profile.getPhone());
        localRecord.put("website", profile.getWebsite());
        localRecord.put("searchQuery", profile.getSearchQuery());
        localRecord.put("age", profile.getAge());
        localRecord.put("favouriteColor", profile.getFavouriteColor());
        localRecord.put("hatedColor", profile.getHatedColor());
        localRecord.put("skillLevel", profile.getSkillLevel());
        localRecord.put("dateOfBirth", profile.getDob());
        localRecord.put("preferredSlot", profile.getSlot());
        localRecord.put("graduationMonth", profile.getGraduationMonth());
        localRecord.put("week", profile.getWeek());
        localRecord.put("meetingTime", profile.getMeetingTime());
        localRecord.put("country", profile.getCountry());
        localRecord.put("languages", profile.getLanguages());
        localRecord.put("framework", profile.getFramework());
        localRecord.put("gender", profile.getGender());
        localRecord.put("interests", profile.getInterests());
        localRecord.put("status", profile.getStatus().name());
        localRecord.put("savedFiles", Map.of(
                "profilePhoto", profile.getProfilePhotoUrl() != null ? profile.getProfilePhotoUrl() : "None",
                "introVideo", profile.getIntroVideoUrl() != null ? profile.getIntroVideoUrl() : "None",
                "resume", profile.getResumeUrl() != null ? profile.getResumeUrl() : "None",
                "audio", profile.getAudioUrl() != null ? profile.getAudioUrl() : "None"
        ));
        localRecord.put("savedAt", LocalDateTime.now().toString());

        String jsonContent = objectMapper.writeValueAsString(localRecord);
        String localJsonPath = storageService.storeText(
                jsonContent,
                "submissions",
                "submission_" + profile.getId() + ".json"
        );
        profile.setLocalDataFilePath("/uploads/" + localJsonPath);
        profile = profileRepository.save(profile);

        return toResponse(profile, jsonContent);
    }

    @Transactional(readOnly = true)
    public List<ProspectResponse> getAllSubmissions() {
        return profileRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(p -> toResponse(p, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ProspectResponse> getSubmissionById(Long id) {
        return profileRepository.findById(id).map(p -> {
            String jsonContent = null;
            if (p.getLocalDataFilePath() != null) {
                try {
                    jsonContent = storageService.readText(p.getLocalDataFilePath());
                } catch (Exception ignored) {
                }
            }
            return toResponse(p, jsonContent);
        });
    }

    @Transactional(readOnly = true)
    public String getLocalFileContent(Long id) {
        ProspectProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id " + id));
        if (profile.getLocalDataFilePath() == null) {
            return "{}";
        }
        try {
            return storageService.readText(profile.getLocalDataFilePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read local file: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteSubmission(Long id) {
        ProspectProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id " + id));

        try {
            storageService.delete(profile.getProfilePhotoUrl());
            storageService.delete(profile.getIntroVideoUrl());
            storageService.delete(profile.getResumeUrl());
            storageService.delete(profile.getAudioUrl());
            storageService.delete(profile.getLocalDataFilePath());
        } catch (IOException ignored) {
        }

        ProspectAuth user = profile.getUser();
        profileRepository.delete(profile);
        if (user != null) {
            authRepository.delete(user);
        }
    }

    private ProspectResponse toResponse(ProspectProfile p, String jsonContent) {
        return ProspectResponse.builder()
                .id(p.getId())
                .userId(p.getUser() != null ? p.getUser().getId() : null)
                .email(p.getUser() != null ? p.getUser().getEmail() : null)
                .fullName(p.getFullName())
                .bio(p.getBio())
                .phone(p.getPhone())
                .website(p.getWebsite())
                .search(p.getSearchQuery())
                .age(p.getAge())
                .favouriteColor(p.getFavouriteColor())
                .hatedColor(p.getHatedColor())
                .skillLevel(p.getSkillLevel())
                .dob(p.getDob())
                .slot(p.getSlot())
                .graduationMonth(p.getGraduationMonth())
                .week(p.getWeek())
                .meetingTime(p.getMeetingTime())
                .country(p.getCountry())
                .languages(p.getLanguages())
                .framework(p.getFramework())
                .gender(p.getGender())
                .interests(p.getInterests())
                .profilePhotoUrl(p.getProfilePhotoUrl())
                .introVideoUrl(p.getIntroVideoUrl())
                .audioUrl(p.getAudioUrl())
                .resumeUrl(p.getResumeUrl())
                .localDataFilePath(p.getLocalDataFilePath())
                .localTextContent(jsonContent)
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
