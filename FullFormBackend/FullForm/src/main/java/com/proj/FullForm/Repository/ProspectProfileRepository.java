package com.proj.FullForm.Repository;

import com.proj.FullForm.Model.ProspectProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import com.proj.FullForm.Model.ProspectAuth;
import java.util.List;
import java.util.Optional;

public interface ProspectProfileRepository extends JpaRepository<ProspectProfile,Long> {
    Optional<ProspectProfile> findByUser(ProspectAuth user);
    Optional<ProspectProfile> findByUserId(Long userId);
    List<ProspectProfile> findAllByOrderByCreatedAtDesc();
}
