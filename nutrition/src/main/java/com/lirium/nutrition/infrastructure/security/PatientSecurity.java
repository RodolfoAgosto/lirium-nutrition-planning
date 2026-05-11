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

    @Autowired
    private PatientProfileRepository patientRepository;
    @Autowired
    private UserRepository userRepository;


    public boolean isOwner(Long patientProfileId, Authentication authentication) {
        // 1. Obtener el username (o ID) del usuario logueado
        UserDetails currentUser = (UserDetails)authentication.getPrincipal();
        User user = userRepository.findByEmail(currentUser.getUsername()).orElseThrow();

        // 2. Buscar el paciente y ver si su "owner" coincide con el usuario
        // Aquí asumimos que tu entidad Patient tiene un campo 'user' con un 'username'
        return patientRepository.findById(patientProfileId)
                .map(p -> p.getUser().getId().equals(user.getId()))
                .orElse(false);
    }
}