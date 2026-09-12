package br.com.queue.infra.unit;

public class UnitIsPresentException extends RuntimeException {
    public UnitIsPresentException(String message) {
        super(message);
    }
}
