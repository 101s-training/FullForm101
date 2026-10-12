package com.proj.FullForm.Service;

import com.proj.FullForm.Model.ProspectAuth;
import com.proj.FullForm.Repository.ProspectAuthRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class ProspectAuthService {

    private final ProspectAuthRepository prospectAuthRepository;
    private final PasswordEncoder passwordEncoder;

    //DI
    public ProspectAuthService(ProspectAuthRepository prospectAuthRepository,
                           PasswordEncoder passwordEncoder) {
        this.prospectAuthRepository = prospectAuthRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //checking if password is correct
    public Boolean getProspect(ProspectAuth prospectAuth){
        if (prospectAuth == null || prospectAuth.getEmail() == null || prospectAuth.getPassword() == null) {
            return false;
        }
        return prospectAuthRepository.findByEmail(prospectAuth.getEmail())
                .map(user -> passwordEncoder.matches(prospectAuth.getPassword(), user.getPassword()))
                .orElse(false);
    }

    public Optional<ProspectAuth> findByEmail(String email) {
        return prospectAuthRepository.findByEmail(email);
    }

    //hashing an adding passwords
    public void postProspect(ProspectAuth prospectAuth) {
        prospectAuth.setPassword(passwordEncoder.encode(prospectAuth.getPassword()));
        prospectAuthRepository.save(prospectAuth);
    }


   //adminOnly
    public ProspectAuth putProspectById(Long id, ProspectAuth req) {
         ProspectAuth existing = prospectAuthRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prospect not found with id " + id));

             existing.setEmail(req.getEmail());
             existing.setPassword(req.getPassword());
         return prospectAuthRepository.save(existing);
    }

    //adminOnly
    public void deleteProspectById(Long id) {

        prospectAuthRepository.deleteById(id);
   }

    //checking for a new user
    public boolean isNewUser(String email) {
        if(prospectAuthRepository.existsByEmail(email)){
            return false;
        }
        else{
           return true;
        }
    }
}
