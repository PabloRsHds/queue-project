package br.com.queue.infra.user;

public class UserUnitMismatchException extends RuntimeException {
    public UserUnitMismatchException(String message) {
        super(message);
    }
}
