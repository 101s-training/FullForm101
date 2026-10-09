package com.proj.FullForm.Controller;

import com.proj.FullForm.Model.ProspectProfile;
import com.proj.FullForm.Repository.ProspectProfileRepository;
import com.proj.FullForm.Service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileMediaController {

    private static final Set<String> IMAGES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> VIDEOS = Set.of("video/mp4", "video/webm", "video/quicktime");
    private static final Set<String> AUDIOS = Set.of("audio/mpeg", "audio/wav", "audio/mp4");

    private final FileStorageService storage;
    private final ProspectProfileRepository profileRepository;

    public ProfileMediaController(FileStorageService storage,
                                  ProspectProfileRepository profileRepository) {
        this.storage = storage;
        this.profileRepository = profileRepository;
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<String> uploadPhoto(@PathVariable Long id,
                                              @RequestParam("file") MultipartFile file) throws IOException {
        ProspectProfile profile = findProfile(id);
        storage.delete(profile.getProfilePhotoUrl());
        profile.setProfilePhotoUrl(storage.store(file, "images", IMAGES));
        profileRepository.save(profile);
        return ResponseEntity.ok(profile.getProfilePhotoUrl());
    }

    @PostMapping("/{id}/video")
    public ResponseEntity<String> uploadVideo(@PathVariable Long id,
                                              @RequestParam("file") MultipartFile file) throws IOException {
        ProspectProfile profile = findProfile(id);
        storage.delete(profile.getIntroVideoUrl());
        profile.setIntroVideoUrl(storage.store(file, "videos", VIDEOS));
        profileRepository.save(profile);
        return ResponseEntity.ok(profile.getIntroVideoUrl());
    }

    @PostMapping("/{id}/audio")
    public ResponseEntity<String> uploadAudio(@PathVariable Long id,
                                              @RequestParam("file") MultipartFile file) throws IOException {
        ProspectProfile profile = findProfile(id);
        storage.delete(profile.getAudioUrl());
        profile.setAudioUrl(storage.store(file, "audio", AUDIOS));
        profileRepository.save(profile);
        return ResponseEntity.ok(profile.getAudioUrl());
    }

    private ProspectProfile findProfile(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Profile not found with id " + id));
    }
}