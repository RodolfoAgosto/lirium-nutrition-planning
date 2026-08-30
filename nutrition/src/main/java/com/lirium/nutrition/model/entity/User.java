package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
public class User extends Auditable implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "email", nullable = false, unique = true, length = 100)
  private String email;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "first_name", length = 60)
  private String firstName;

  @Column(name = "last_name", length = 60)
  private String lastName;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(name = "dni", unique = true, length = 20)
  private String dni;

  @Column(name = "email_validated", nullable = false)
  private Boolean emailValidated = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "role")
  private Role role;

  @Column(name = "enabled", nullable = false)
  private boolean enabled = true;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private PatientProfile patientProfile;

  public User() {}

  public User(String email, String passwordHash, String firstName, String lastName, Role role) {
    this();
    if (role == Role.PATIENT) {
      this.setPatientProfile(new PatientProfile(this));
    }
    this.email = Objects.requireNonNull(email);
    this.passwordHash = Objects.requireNonNull(passwordHash);
    this.firstName = firstName;
    this.lastName = lastName;
    this.role = role;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return role.getAuthorities();
  }

  @Override
  public String getPassword() {
    return this.passwordHash;
  }

  @Override
  public String getUsername() {
    return this.email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User user)) return false;
    return id != null && Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
