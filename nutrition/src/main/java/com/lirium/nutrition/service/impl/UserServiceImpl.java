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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
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

        log.info("Registering user email={}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("User registration failed - email already exists email={}", request.email());
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(hashPassword(request.password()));
        User savedUser = userRepository.save(user);

        log.info("User registered successfully id={} email={}", savedUser.getId(), savedUser.getEmail());

        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDTO registerPatient(CreatePatientRequestDTO request) {

        log.info("Registering patient email={}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Patient registration failed - email already exists email={}", request.email());
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        log.info("Patient registered successfully id={} email={}", savedUser.getId(), savedUser.getEmail());

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

        log.info("Updating user id={}", id);

        User user = getUserOrThrow(id);

        if (log.isDebugEnabled()) {
            log.debug("Update payload: {}", request);
        }

        userMapper.updateUserFromDTO(request, user);

        log.info("User updated successfully id={}", id);

        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO setEnabled(Long id, boolean enabled) {

        log.info("Setting enabled={} for user id={}", enabled, id);

        User user = getUserOrThrow(id);
        user.setEnabled(enabled);
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO validateEmail(Long id) {

        log.info("Validating email for user id={}", id);
        User user = getUserOrThrow(id);
        user.setEmailValidated(true);
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {

        log.info("Disabling user id={}", id);

        User user = getUserOrThrow(id);

        if (!user.isEnabled()) {
            log.warn("User disable failed - already disabled id={}", id);
            throw new AccountDisabledException(id);
        }

        user.setEnabled(false);

        log.info("User disabled successfully id={}", id);

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