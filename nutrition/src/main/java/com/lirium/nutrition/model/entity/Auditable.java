package com.lirium.nutrition.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
@Getter
public abstract class Auditable {

  @org.springframework.data.annotation.CreatedDate
  @Column(nullable = false, updatable = false, name = "created_at")
  private java.time.LocalDateTime createdAt;

  @org.springframework.data.annotation.LastModifiedDate
  @Column(nullable = false, name = "updated_at")
  private java.time.LocalDateTime updatedAt;

  @org.springframework.data.annotation.CreatedBy
  @Column(updatable = false, name = "created_by")
  private String createdBy;

  @org.springframework.data.annotation.LastModifiedBy
  @Column(name = "updated_by")
  private String updatedBy;
}
