package org.example.gateway.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.shared.response.GenericResponse;

@Getter
@Setter
@NoArgsConstructor
public class RefreshResponse extends GenericResponse {

    private String token;
    private String type;
    private Long expiresIn;

    public RefreshResponse(String token, String type, Long expiresIn) {
        this.token = token;
        this.type = type;
        this.expiresIn = expiresIn;
    }
}
