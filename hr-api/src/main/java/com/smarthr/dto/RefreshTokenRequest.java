package com.smarthr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Le token de rafraîchissement est obligatoire")
    private String refreshToken;
}
