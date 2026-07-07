package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("patientSecurity")
public class PatientSecurity {

     public boolean isOwner(Long patientProfileId, Authentication authentication) {
         User principal = (User) authentication.getPrincipal();
         return principal.getId().equals(patientProfileId);
     }

}