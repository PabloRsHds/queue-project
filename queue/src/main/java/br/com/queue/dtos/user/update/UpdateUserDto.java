package br.com.queue.dtos.user.update;

import java.util.HashSet;

public record UpdateUserDto(

        String userId,
        String username,
        String name,
        String surname,
        String phone,
        String email,
        String password,
        String role,
        Boolean active,
        Integer counterNumber,
        HashSet<String> serviceIds
) {
}
