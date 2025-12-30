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
            assertThat(dto)
                    .extracting(UserDTO.AuthorDTO::getId, UserDTO.AuthorDTO::getUsername, UserDTO.AuthorDTO::getDisplayName)
                    .containsExactly(1, "grummans", "Grummans");
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
            assertThat(dto)
                    .extracting(UserDTO.AuthorDTO::getId, UserDTO.AuthorDTO::getUsername, UserDTO.AuthorDTO::getDisplayName)
                    .containsExactly(2, "john", "John Doe");
        }

        @Test
        @DisplayName("Should have working equals and hashCode")
        void shouldHaveWorkingEqualsAndHashCode() {
            // Given
            UserDTO.AuthorDTO dto1 = new UserDTO.AuthorDTO(1, "grummans", "Grummans");
            UserDTO.AuthorDTO dto2 = new UserDTO.AuthorDTO(1, "grummans", "Grummans");
            UserDTO.AuthorDTO dto3 = new UserDTO.AuthorDTO(2, "john", "John");

            // Then
            assertThat(dto1)
                    .isEqualTo(dto2)
                    .isNotEqualTo(dto3)
                    .hasSameHashCodeAs(dto2);
        }

        @Test
        @DisplayName("Should have working toString")
        void shouldHaveWorkingToString() {
            // Given
            UserDTO.AuthorDTO dto = new UserDTO.AuthorDTO(1, "grummans", "Grummans");

            // When
            String toString = dto.toString();

            // Then
            assertThat(toString)
                    .contains("grummans")
                    .contains("Grummans");
        }
    }
}

