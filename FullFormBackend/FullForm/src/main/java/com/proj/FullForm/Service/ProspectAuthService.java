package com.proj.FullForm.Service;

import com.proj.FullForm.Model.ProspectAuth;
import com.proj.FullForm.Repository.ProspectAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProspectAuthService {

    private final ProspectAuthRepository prospectAuthRepository;


    public Optional<ProspectAuth> getProspectById(Long id) {
        return prospectAuthRepository.findById(id);
    }

    public void  postProspect(ProspectAuth prospectAuth) {
        prospectAuthRepository.save(prospectAuth);
    }

    public ProspectAuth putProspectById(Long id, ProspectAuth req) {
         ProspectAuth existing = prospectAuthRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prospect not found with id " + id));

             existing.setEmail(req.getEmail());
             existing.setPassword(req.getPassword());
         return prospectAuthRepository.save(existing);
    }

    public void deleteProspectById(Long id) {

        prospectAuthRepository.deleteById(id);
    }
}
