package com.grummans.noyblog.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserDTO Tests")
class UserDTOTest {

    @Nested
    @DisplayName("UserDTO.AuthorDTO Tests")
    class AuthorDTOTests {

        @Test
        @DisplayName("Should create AuthorDTO with all fields")
        void shouldCreateAuthorDTOWithAllFields() {
            // When
            UserDTO.AuthorDTO dto = new UserDTO.AuthorDTO(1, "grummans", "Grummans");

            // Then
            assertThat(dto.getId()).isEqualTo(1);
            assertThat(dto.getUsername()).isEqualTo("grummans");
            assertThat(dto.getDisplayName()).isEqualTo("Grummans");
        }

        @Test
        @DisplayName("Should create AuthorDTO with no-args constructor")
        void shouldCreateAuthorDTOWithNoArgsConstructor() {
            // When
            UserDTO.AuthorDTO dto = new UserDTO.AuthorDTO();
            dto.setId(2);
            dto.setUsername("john");
            dto.setDisplayName("John Doe");

            // Then
            assertThat(dto.getId()).isEqualTo(2);
            assertThat(dto.getUsername()).isEqualTo("john");
            assertThat(dto.getDisplayName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Should have working equals and hashCode")
        void shouldHaveWorkingEqualsAndHashCode() {
            // Given
            UserDTO.AuthorDTO dto1 = new UserDTO.AuthorDTO(1, "grummans", "Grummans");
            UserDTO.AuthorDTO dto2 = new UserDTO.AuthorDTO(1, "grummans", "Grummans");
            UserDTO.AuthorDTO dto3 = new UserDTO.AuthorDTO(2, "john", "John");

            // Then
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1).isNotEqualTo(dto3);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }

        @Test
        @DisplayName("Should have working toString")
        void shouldHaveWorkingToString() {
            // Given
            UserDTO.AuthorDTO dto = new UserDTO.AuthorDTO(1, "grummans", "Grummans");

            // When
            String toString = dto.toString();

            // Then
            assertThat(toString).contains("grummans");
            assertThat(toString).contains("Grummans");
        }
    }
}

