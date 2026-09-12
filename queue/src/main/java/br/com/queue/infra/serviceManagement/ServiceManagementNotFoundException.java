package br.com.queue.infra.serviceManagement;

public class ServiceManagementNotFoundException extends RuntimeException {
    public ServiceManagementNotFoundException(String message) {
        super(message);
    }
}
