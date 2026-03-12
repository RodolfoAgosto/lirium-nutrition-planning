package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.exception.AccountDisabledException;
import com.lirium.nutrition.exception.EmailAlreadyExistsException;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.UserMapper;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.UserRepository;
import com.lirium.nutrition.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponseDTO registerUser(CreateUserRequestDTO request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = userMapper.toEntity(request);
        user.setPasswordHash(hashPassword(request.password()));
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDTO registerPatient(CreatePatientRequestDTO request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }


    @Override
    public UserResponseDTO findById(Long id) {
        User user = getUserOrThrow(id);
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return userMapper.toResponseDTO(user);
    }

    @Override
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public UserResponseDTO updateBasicInfo(Long id, UserUpdateRequestDTO request) {
        User user = getUserOrThrow(id);
        userMapper.updateUserFromDTO(request, user);
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO setEnabled(Long id, boolean enabled) {
        User user = getUserOrThrow(id);
        user.setEnabled(enabled);
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO validateEmail(Long id) {
        User user = getUserOrThrow(id);
        user.setEmailValidated(true);
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        User user = getUserOrThrow(id);
        if (!user.isEnabled()) {
            throw new AccountDisabledException(id);
        }
        user.setEnabled(false);
    }

    private String hashPassword(String password) {
        Objects.requireNonNull(password,"Password cannot be null.");
        return passwordEncoder.encode(password);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

}