package br.com.queue.dtos.customer.create;

public record ResponseCustomerDto(
        String customerId,
        String name,
        String cpf,
        String rg,
        String phone,
        String email,
        String createdAt,
        String updatedAt
) {
}
