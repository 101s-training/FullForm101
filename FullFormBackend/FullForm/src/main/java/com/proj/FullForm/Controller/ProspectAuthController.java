package com.proj.FullForm.Controller;

import com.proj.FullForm.Model.ProspectAuth;
import com.proj.FullForm.Service.ProspectAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class ProspectAuthController {

    private final ProspectAuthService prospectAuthService;

    public ProspectAuthController(ProspectAuthService prospectAuthService) {
        this.prospectAuthService = prospectAuthService;
    }

    // User login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody ProspectAuth req) {
        if (req == null || req.getEmail() == null || req.getPassword() == null ||
                req.getEmail().isBlank() || req.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Email and password are required."));
        }

        String email = req.getEmail().trim().toLowerCase();
        req.setEmail(email);

        if (prospectAuthService.isNewUser(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "No user found with this email. Please create an account."));
        }

        Boolean isValid = prospectAuthService.getProspect(req);
        if (Boolean.TRUE.equals(isValid)) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "email", email,
                    "message", "Login successful."
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Incorrect password. Please try again."));
        }
    }

    // New user registration
    @PostMapping({"", "/register", "/signup"})
    public ResponseEntity<?> addAuthDeets(@RequestBody ProspectAuth req) {
        if (req == null || req.getEmail() == null || req.getPassword() == null ||
                req.getEmail().isBlank() || req.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Email and password are required."));
        }

        String email = req.getEmail().trim().toLowerCase();
        req.setEmail(email);

        if (!prospectAuthService.isNewUser(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", "Email is already registered. Please log in instead."));
        }

        prospectAuthService.postProspect(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "email", email,
                "message", "Account registered successfully."
        ));
    }

    // Old user login / credential verification via GET (for backward compatibility)
    @GetMapping
    public ResponseEntity<?> fetchAuthDeets(
            @RequestParam(value = "email", required = false) String emailParam,
            @RequestParam(value = "password", required = false) String passwordParam,
            @RequestBody(required = false) ProspectAuth reqBody) {

        String email = (reqBody != null && reqBody.getEmail() != null) ? reqBody.getEmail() : emailParam;
        String password = (reqBody != null && reqBody.getPassword() != null) ? reqBody.getPassword() : passwordParam;

        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Email is required"));
        }

        email = email.trim().toLowerCase();
        ProspectAuth auth = new ProspectAuth();
        auth.setEmail(email);
        auth.setPassword(password != null ? password : "");

        if (!prospectAuthService.isNewUser(email)) {
            Boolean res = prospectAuthService.getProspect(auth);
            if (Boolean.TRUE.equals(res)) {
                return ResponseEntity.ok(Map.of("success", true, "email", email));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "User not found"));
        }
    }

    // For admin access only
    @PutMapping("/{id}")
    public ResponseEntity<ProspectAuth> updateAuthDeets(@PathVariable Long id, @Valid @RequestBody ProspectAuth req) {
        ProspectAuth updated = prospectAuthService.putProspectById(id, req);
        return ResponseEntity.ok(updated);
    }

    // For admin access only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthDeets(@PathVariable Long id) {
        prospectAuthService.deleteProspectById(id);
        return ResponseEntity.ok().build();
    }
}
