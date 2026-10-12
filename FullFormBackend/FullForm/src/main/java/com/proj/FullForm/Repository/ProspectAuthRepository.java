package com.proj.FullForm.Repository;

import com.proj.FullForm.Model.ProspectAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProspectAuthRepository extends JpaRepository<ProspectAuth,Long> {
    Optional<ProspectAuth> findByEmail(String email);
}
