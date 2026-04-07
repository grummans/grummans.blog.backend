package com.grummans.noyblog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Users Model Tests")
class UsersTest {

    @Test
    @DisplayName("Should create Users using builder")
    void shouldCreateUsersUsingBuilder() {
        // When
        Users user = Users.builder()
                .id(1)
                .username("admin")
                .email("admin@example.com")
                .password("hashedPassword")
                .displayName("Grummans")
                .bio("A developer")
                .avatarUrl("https://example.com/avatar.jpg")
                .role("ADMIN")
                .isActive(true)
                .build();

        // Then
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getEmail()).isEqualTo("admin@example.com");
        assertThat(user.getPassword()).isEqualTo("hashedPassword");
        assertThat(user.getDisplayName()).isEqualTo("Grummans");
        assertThat(user.getBio()).isEqualTo("A developer");
        assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(user.getRole()).isEqualTo("ADMIN");
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should create Users using no-args constructor")
    void shouldCreateUsersUsingNoArgsConstructor() {
        // When
        Users user = new Users();
        user.setId(2);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("hash123");
        user.setDisplayName("John Doe");
        user.setRole("USER");
        user.setActive(true);

        // Then
        assertThat(user.getId()).isEqualTo(2);
        assertThat(user.getUsername()).isEqualTo("john");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getDisplayName()).isEqualTo("John Doe");
        assertThat(user.getRole()).isEqualTo("USER");
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should handle null optional fields")
    void shouldHandleNullOptionalFields() {
        // When
        Users user = Users.builder()
                .id(1)
                .username("test")
                .email("test@example.com")
                .password("hash")
                .displayName("Test")
                .bio(null)
                .avatarUrl(null)
                .build();

        // Then
        assertThat(user.getBio()).isNull();
        assertThat(user.getAvatarUrl()).isNull();
    }

    @Test
    @DisplayName("Should handle inactive user")
    void shouldHandleInactiveUser() {
        // When
        Users user = Users.builder()
                .id(1)
                .username("inactive")
                .isActive(false)
                .build();

        // Then
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should set and get timestamps")
    void shouldSetAndGetTimestamps() {
        // Given
        Users user = new Users();

        // When
        user.setCreatedAt("2024-01-01T00:00:00");
        user.setUpdatedAt("2024-01-02T00:00:00");

        // Then
        assertThat(user.getCreatedAt()).isEqualTo("2024-01-01T00:00:00");
        assertThat(user.getUpdatedAt()).isEqualTo("2024-01-02T00:00:00");
    }
}

