package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;

import java.util.List;

public interface UserService {

    UserResponseDTO registerPatient(CreateUserRequestDTO userCreate);

    UserResponseDTO findById(Long id);

    UserResponseDTO findByEmail(String email);

    List<UserResponseDTO> findAll();

    UserResponseDTO updateBasicInfo(Long id, UpdateUserRequestDTO request);

    UserResponseDTO setEnabled(Long id, boolean enabled);

    UserResponseDTO validateEmail(Long id);

    void deleteById(Long id);

}