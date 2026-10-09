package com.proj.FullForm.Controller;


import com.proj.FullForm.Model.ProspectAuth;
import com.proj.FullForm.Service.ProspectAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class ProspectAuthController {

    private final ProspectAuthService prospectAuthService;


    public ProspectAuthController(ProspectAuthService prospectAuthService) {
        this.prospectAuthService = prospectAuthService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<ProspectAuth>> fetchAuthDeets(@PathVariable Long  id){
        return ResponseEntity.status(200).body(prospectAuthService.getProspectById(id));
    }

    @PostMapping
    public ResponseEntity<ProspectAuth> addAuthDeets(@RequestBody ProspectAuth req){
        prospectAuthService.postProspect(req);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProspectAuth> updateAuthDeets(@PathVariable Long id,@Valid @RequestBody ProspectAuth req){
        prospectAuthService.putProspectById(id,req);
        return ResponseEntity.status(200).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProspectAuth> deleteAuthDeets(@PathVariable Long id){
         prospectAuthService.deleteProspectById(id);
        return ResponseEntity.status(200).build();
    }
}
