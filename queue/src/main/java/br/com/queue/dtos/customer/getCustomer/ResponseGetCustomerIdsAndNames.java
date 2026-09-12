package br.com.queue.dtos.customer.getCustomer;

public record ResponseGetCustomerIdsAndNames(
        String customerId,
        String name
) {
}
