package com.proj.FullForm.Controller;

import com.proj.FullForm.Model.AuditionStatus;
import com.proj.FullForm.Model.ProspectAuth;
import com.proj.FullForm.Model.ProspectProfile;
import com.proj.FullForm.Service.ProspectProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/profile")
public class ProspectProfileController {

    private final ProspectProfileService prospectProfileService;

    public ProspectProfileController(ProspectProfileService prospectProfileService) {
        this.prospectProfileService = prospectProfileService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<Optional<ProspectProfile>> fetchAuthDeets(@PathVariable Long  id){
        return ResponseEntity.status(200).body(prospectProfileService.getProspectById(id));
    }

    @PostMapping
    public ResponseEntity<ProspectProfile> addAuthDeets(@RequestBody ProspectProfile req){
        req.setStatus(AuditionStatus.DRAFT);
        prospectProfileService.postProspect(req);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProspectProfile> updateAuthDeets(@PathVariable Long id,@Valid @RequestBody ProspectProfile req){
        prospectProfileService.putProspectById(id,req);
        return ResponseEntity.status(200).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProspectProfile> deleteAuthDeets(@PathVariable Long id){
        prospectProfileService.deleteProspectById(id);
        return ResponseEntity.status(200).build();
    }
}
