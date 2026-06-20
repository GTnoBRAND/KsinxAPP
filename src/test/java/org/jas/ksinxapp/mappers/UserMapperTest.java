package org.jas.ksinxapp.mappers;

import org.jas.ksinxapp.dtos.StudentRegistrationRequest;
import org.jas.ksinxapp.dtos.StudentResponse;
import org.jas.ksinxapp.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapperImpl();

    @Test
    void toEntity_copiesNameAndEmail_andIgnoresSecureFields() {
        var request = new StudentRegistrationRequest("Jane Doe", "jane@example.com", "supersecret");

        User user = mapper.toEntity(request);

        assertThat(user.getFullName()).isEqualTo("Jane Doe");
        assertThat(user.getEmail()).isEqualTo("jane@example.com");
        assertThat(user.getId()).isNull();
        assertThat(user.getRole()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getEnrollments()).isNull();
    }

    @Test
    void toResponse_mapsRoleAsString() {
        var user = new User(7L, "ada@example.com", "Ada Lovelace", User.Role.TEACHER, "hash", null, true);

        StudentResponse response = mapper.toResponse(user);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.email()).isEqualTo("ada@example.com");
        assertThat(response.fullName()).isEqualTo("Ada Lovelace");
        assertThat(response.role()).isEqualTo("TEACHER");
    }

    @Test
    void updateEntityFromDto_replacesEditableFields() {
        var user = new User(1L, "old@example.com", "Old Name", User.Role.STUDENT, "hash", null, true);
        var request = new StudentRegistrationRequest("New Name", "new@example.com", "ignored");

        mapper.updateEntityFromDto(request, user);

        assertThat(user.getFullName()).isEqualTo("New Name");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getRole()).isEqualTo(User.Role.STUDENT);
    }
}
