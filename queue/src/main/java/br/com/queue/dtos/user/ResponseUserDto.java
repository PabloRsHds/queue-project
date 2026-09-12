package br.com.queue.dtos.user;

import java.time.LocalDateTime;

public record ResponseUserDto(

        String userId,
        String username,
        String name,
        String surname,
        String phone,
        String email,
        String role,
        Integer counterNumber,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
