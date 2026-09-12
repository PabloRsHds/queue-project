package br.com.queue.infra.schedule;

public class ScheduleDeleteException extends RuntimeException {
    public ScheduleDeleteException(String message) {
        super(message);
    }
}
