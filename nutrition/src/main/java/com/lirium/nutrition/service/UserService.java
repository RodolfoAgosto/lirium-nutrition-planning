package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;

import java.util.List;

public interface UserService {

    UserResponseDTO registerUser(CreateUserRequestDTO userCreate);

    UserResponseDTO registerPatient(CreatePatientRequestDTO userCreate);

    UserResponseDTO findById(Long id);

    UserResponseDTO findByEmail(String email);

    List<UserResponseDTO> findAll();

    UserResponseDTO updateBasicInfo(Long id, UserUpdateRequestDTO request);

    UserResponseDTO setEnabled(Long id, boolean enabled);

    UserResponseDTO validateEmail(Long id);

    void deleteById(Long id);

}