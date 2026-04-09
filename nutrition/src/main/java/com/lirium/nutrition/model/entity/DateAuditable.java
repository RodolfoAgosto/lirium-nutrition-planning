package com.lirium.nutrition.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
@Getter
public abstract class DateAuditable {

    @org.springframework.data.annotation.CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @org.springframework.data.annotation.LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
