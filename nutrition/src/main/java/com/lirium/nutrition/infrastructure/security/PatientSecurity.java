package com.lirium.nutrition.infrastructure.security;


import com.lirium.nutrition.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("patientSecurity")
@RequiredArgsConstructor
public class PatientSecurity {
     public boolean isOwner(Long patientProfileId, Authentication authentication) {
         User principal = (User) authentication.getPrincipal();
         return principal.getId().equals(patientProfileId);
     }
}