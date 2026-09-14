package org.example.gateway.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.shared.response.GenericResponse;

@Getter
@Setter
@NoArgsConstructor
public class RegisterResponse extends GenericResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;

    public RegisterResponse(Long id, String email, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
