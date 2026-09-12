package br.com.queue.dtos.customer.update;

import java.time.LocalDateTime;

public record ResponseUpdateCustomerDto(
        String customerId,
        String name,
        String cpf,
        String rg,
        String phone,
        String email,
        LocalDateTime updateAt
) {
}
