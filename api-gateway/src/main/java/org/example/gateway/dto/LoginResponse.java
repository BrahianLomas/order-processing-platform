package org.example.gateway.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.shared.response.GenericResponse;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse extends GenericResponse {

    private String token;
    private String type;
    private Long expiresIn;
    private UserSummary user;

    public LoginResponse(String token, String type, Long expiresIn, UserSummary user) {
        this.token = token;
        this.type = type;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}
