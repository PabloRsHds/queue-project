package br.com.queue.dtos.loginDto;

public record RequestLoginDto(

        String unitId,

        String emailOrUsername,

        String password
) {
}
