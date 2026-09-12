package br.com.queue.dtos.user.create;

import java.util.HashSet;

public record CreateUserDto(

        String username,
        String name,
        String surname,
        String phone,
        String email,
        String password,
        String role,
        Integer counterNumber,
        HashSet<String> serviceIds
) {
}
