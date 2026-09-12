package br.com.queue.dtos.customer.create;

public record CreateCustomerDto(
        String name,
        String cpf,
        String rg,
        String phone,
        String email
) {
}
