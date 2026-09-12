package br.com.queue.infra.attendances;

public class AttendanceInvalidStateException extends RuntimeException {
    public AttendanceInvalidStateException(String message) {
        super(message);
    }
}
