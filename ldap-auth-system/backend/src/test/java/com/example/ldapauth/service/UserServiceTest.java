package com.example.ldapauth.service;

import com.example.ldapauth.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void getUserFromAuthentication_withValidAuth_shouldReturnUser() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "ben",
            "benspassword",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        User user = userService.getUserFromAuthentication(auth);

        assertThat(user).isNotNull();
        assertThat(user.username()).isEqualTo("ben");
        assertThat(user.fullName()).isEqualTo("Ben Alex");
        assertThat(user.email()).isEqualTo("ben@springframework.org");
    }

    @Test
    public void getUserFromAuthentication_withNullAuth_shouldReturnNull() {
        User user = userService.getUserFromAuthentication(null);
        assertThat(user).isNull();
    }
}