package br.com.queue.dtos.user.get_user;

import java.util.Set;

public record ResponseUserInfoDto(
        String userId,
        String username,
        String name,
        String surname,
        String phone,
        String email,
        String role,
        Integer counterNumber,
        Boolean active,
        String createdAt,
        String updatedAt,
        Set<String> serviceNames
) {
}
