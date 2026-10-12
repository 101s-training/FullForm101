package com.proj.FullForm.Controller;

import com.proj.FullForm.Model.ProspectProfile;
import com.proj.FullForm.Repository.ProspectProfileRepository;
import com.proj.FullForm.Service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileMediaController {

    private final S3Service s3Service;
    private final ProspectProfileRepository profileRepository;

    @PostMapping("/{id}/photo")
    public ResponseEntity<String> uploadPhoto(@PathVariable Long id,
                                              @RequestParam("file") MultipartFile file) throws IOException {
        ProspectProfile profile = findProfile(id);
        if (profile.getProfilePhotoUrl() != null) {
            try { s3Service.delete(profile.getProfilePhotoUrl()); } catch (Exception ignored) {}
        }
        String key = s3Service.upload(file, "images");
        profile.setProfilePhotoUrl(key);
        profileRepository.save(profile);
        return ResponseEntity.ok(s3Service.getViewUrl(key));
    }

    @PostMapping("/{id}/video")
    public ResponseEntity<String> uploadVideo(@PathVariable Long id,
                                              @RequestParam("file") MultipartFile file) throws IOException {
        ProspectProfile profile = findProfile(id);
        if (profile.getIntroVideoUrl() != null) {
            try { s3Service.delete(profile.getIntroVideoUrl()); } catch (Exception ignored) {}
        }
        String key = s3Service.upload(file, "videos");
        profile.setIntroVideoUrl(key);
        profileRepository.save(profile);
        return ResponseEntity.ok(s3Service.getViewUrl(key));
    }

    @PostMapping("/{id}/audio")
    public ResponseEntity<String> uploadAudio(@PathVariable Long id,
                                              @RequestParam("file") MultipartFile file) throws IOException {
        ProspectProfile profile = findProfile(id);
        if (profile.getAudioUrl() != null) {
            try { s3Service.delete(profile.getAudioUrl()); } catch (Exception ignored) {}
        }
        String key = s3Service.upload(file, "audio");
        profile.setAudioUrl(key);
        profileRepository.save(profile);
        return ResponseEntity.ok(s3Service.getViewUrl(key));
    }

    private ProspectProfile findProfile(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Profile not found with id " + id));
    }
}