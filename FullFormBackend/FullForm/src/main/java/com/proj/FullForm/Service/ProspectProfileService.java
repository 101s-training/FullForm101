package com.proj.FullForm.Service;
import com.proj.FullForm.Model.ProspectProfile;
import com.proj.FullForm.Repository.ProspectProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProspectProfileService {

    private final ProspectProfileRepository prospectProfileRepository;


    public Optional<ProspectProfile> getProspectById(Long id) {
        return prospectProfileRepository.findById(id);

    }

    public void  postProspect(ProspectProfile prospectProfile) {
        prospectProfileRepository.save(prospectProfile);
    }

    public ProspectProfile putProspectById(Long id, ProspectProfile req) {
        ProspectProfile existing = prospectProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prospect not found with id " + id));

            existing.setFullName(req.getFullName());
            existing.setBio(req.getBio());
            existing.setProfilePhotoUrl(req.getProfilePhotoUrl());
            existing.setIntroVideoUrl(req.getIntroVideoUrl());
            existing.setAudioUrl(req.getAudioUrl());
            return prospectProfileRepository.save(existing);
    }

    public void deleteProspectById(Long id) {

        prospectProfileRepository.deleteById(id);
    }
}
