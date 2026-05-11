# lirium-nutrition-planning
REST API for nutritional planning, personalized goals, and nutrient calculations.
## Security Architecture

### Authentication
JWT-based stateless authentication. No server-side sessions.

**Flow:**
1. Client sends credentials to `POST /auth/login`
2. Server validates and returns a signed JWT
3. Client includes token in every request:
   `Authorization: Bearer <token>`
4. `JwtAuthenticationFilter` validates the token on each request

---

### Authorization Model
Two-layer authorization strategy:

**Layer 1 — URL-level (SecurityFilterChain)**
Broad role-based rules applied globally before reaching controllers.
Example: any request to `/api/users/**` requires `ROLE_ADMIN`
or `ROLE_NUTRITIONIST`.

**Layer 2 — Method-level (@PreAuthorize)**
Fine-grained rules applied per endpoint, including ownership validation.
Example: a PATIENT can only access their own records.

---

### Roles & Authorities

| Role | Description |
|------|-------------|
| `ADMIN` | Full system access |
| `NUTRITIONIST` | Manages patients, plans and templates |
| `PATIENT` | Read-only access to own data and daily records |

Each role is composed of granular authorities
(e.g. `plan.write`, `record.read`, `user.delete`).

---

### Ownership Rules
Endpoints that expose patient-specific data enforce ownership
at the method level:

- A PATIENT can only retrieve **their own** profile, plans,
  and daily records
- NUTRITIONIST and ADMIN can access **any** patient's data

Example:
```java
@PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or 
               #patientId == authentication.principal.id")
```

---

### Password Security
Passwords are hashed using **BCrypt** before storage.
Plain-text passwords are never persisted or logged.

---

### CORS
Configured to allow requests only from the registered
frontend origin. Credentials are supported for
authenticated requests.