package br.com.queue.dtos.tokenDto;

import jakarta.validation.constraints.NotBlank;

public record RequestRefreshTokenDto(

        @NotBlank(message = "The refreshToken cannot be blank")
        String refreshToken
) {
}
