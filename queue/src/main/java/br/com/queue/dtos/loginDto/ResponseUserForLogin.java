package br.com.queue.dtos.loginDto;

public record ResponseUserForLogin(

        String userId,
        String password,
        String role,
        String unitId
) {
}
