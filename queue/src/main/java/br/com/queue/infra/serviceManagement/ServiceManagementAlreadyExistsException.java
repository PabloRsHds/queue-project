package br.com.queue.infra.serviceManagement;

public class ServiceManagementAlreadyExistsException extends RuntimeException {
    public ServiceManagementAlreadyExistsException(String message) {
        super(message);
    }
}
