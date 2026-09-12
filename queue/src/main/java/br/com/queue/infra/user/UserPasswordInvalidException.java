package br.com.queue.infra.user;

public class UserPasswordInvalidException extends RuntimeException {
    public UserPasswordInvalidException(String message) {
        super(message);
    }
}
