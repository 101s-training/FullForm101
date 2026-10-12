package com.proj.FullForm.Controller;

import com.proj.FullForm.DTO.ProspectResponse;
import com.proj.FullForm.Service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProspectResponse> submitForm(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "website", required = false) String website,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "age", required = false) Integer age,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "hatedColor", required = false) String hatedColor,
            @RequestParam(value = "skill", required = false) Integer skill,
            @RequestParam(value = "dob", required = false) String dob,
            @RequestParam(value = "slot", required = false) String slot,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "week", required = false) String week,
            @RequestParam(value = "meeting", required = false) String meeting,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "languages", required = false) List<String> languages,
            @RequestParam(value = "framework", required = false) String framework,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "interests", required = false) List<String> interests,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "video", required = false) MultipartFile video,
            @RequestParam(value = "resume", required = false) MultipartFile resume,
            @RequestParam(value = "audio", required = false) MultipartFile audio
    ) throws IOException {

        ProspectResponse saved = submissionService.saveSubmission(
                name, email, password, phone, website, search, bio, age,
                color, hatedColor, skill, dob, slot, month, week, meeting,
                country, languages, framework, gender, interests,
                photo, video, resume, audio
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ProspectResponse>> listAllSubmissions() {
        return ResponseEntity.ok(submissionService.getAllSubmissions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProspectResponse> getSubmission(@PathVariable Long id) {
        return submissionService.getSubmissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/{id}/local-file", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getLocalFileContent(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getLocalFileContent(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id) {
        submissionService.deleteSubmission(id);
        return ResponseEntity.noContent().build();
    }
}
