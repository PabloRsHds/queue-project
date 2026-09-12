package br.com.queue.dtos.customer.getCustomer;

public record ResponseCustomerById(

        String customerId,
        String name,
        String cpf,
        String rg,
        String phone,
        String email,
        String createdAt,
        String updatedAt,
        String ticketCode
) {
}
