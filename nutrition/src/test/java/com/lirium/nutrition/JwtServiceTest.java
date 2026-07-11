package com.lirium.nutrition;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

//    @Value("${app.jwt.secret}")
//    private String secret;
//
//    @Autowired
//    private JwtService jwtService;
//
//    @Test
//    void generateAndValidateToken() {
//        UserDetails user = new org.springframework.security.core.userdetails.User(
//                "test@test.com", "password", List.of()
//        );
//        String token = jwtService.generateToken(user);
//        assertNotNull(token);
//        assertEquals("test@test.com", jwtService.extractUsername(token));
//        assertTrue(jwtService.isTokenValid(token, user));
//    }
//

}