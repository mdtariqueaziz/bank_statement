package com.tarique.bankstatement.dto.response;

import lombok.*;

/**
 * Returned after successful authentication — carries the JWT access token.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    private String  accessToken;
    private String  tokenType = "Bearer";
    private long    expiresIn;
    private String  username;
    private String  email;
    private String  role;
}
