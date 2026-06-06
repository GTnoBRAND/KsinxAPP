package org.jas.ksinxapp.mappers;

import org.jas.ksinxapp.dtos.LoginRequest;
import org.jas.ksinxapp.dtos.LoginResponse;
import org.jas.ksinxapp.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginMapperTest {

    private final LoginMapper mapper = new LoginMapperImpl();

    @Test
    void entity_copiesEmail_andIgnoresOtherFields() {
        var request = new LoginRequest("jane@example.com", "password");

        User user = mapper.entity(request);

        assertThat(user.getEmail()).isEqualTo("jane@example.com");
        assertThat(user.getId()).isNull();
        assertThat(user.getRole()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getEnrollments()).isNull();
    }

    @Test
    void loginResponse_combinesUserAndToken() {
        var user = new User(7L, "ada@example.com", "Ada", User.Role.ADMIN, "hash", null);

        LoginResponse response = mapper.loginResponse(user, "abc.def.ghi");

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.fullName()).isEqualTo("Ada");
        assertThat(response.role()).isEqualTo("ADMIN");
        assertThat(response.token()).isEqualTo("abc.def.ghi");
    }
}
