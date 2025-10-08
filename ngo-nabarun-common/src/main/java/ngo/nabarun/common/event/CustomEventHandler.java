package ngo.nabarun.common.event;

public interface CustomEventHandler<T> {
    void handle(T event) throws Exception;
}
