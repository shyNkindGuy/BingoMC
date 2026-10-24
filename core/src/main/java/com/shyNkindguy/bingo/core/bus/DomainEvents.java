package com.shyNkindguy.bingo.core.bus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// Bus de eventos sincrónico: quien se suscribe a un tipo recibe los eventos de ese tipo en el mismo hilo que
// los publica, en el orden en que se suscribió. Si un suscriptor falla, los demás lo reciben igual y el
// error va a `onError` (por defecto se relanza el primero después de entregarlo a todos).
public final class DomainEvents {

    // Para dejar de recibir (otro plugin que se apaga).
    public interface Subscription {
        void cancel();
    }

    private final List<Listener<?>> subscriptions = new CopyOnWriteArrayList<>();
    private final BiConsumer<DomainEvent, RuntimeException> onError;

    public DomainEvents() {
        this(null);
    }

    public DomainEvents(BiConsumer<DomainEvent, RuntimeException> onError) {
        this.onError = onError;
    }

    public <E extends DomainEvent> Subscription subscribe(Class<E> type, Consumer<? super E> subscriber) {
        Listener<E> listener = new Listener<>(type, subscriber);
        subscriptions.add(listener);
        return () -> subscriptions.remove(listener);
    }

    public void publish(DomainEvent event) {
        RuntimeException first = null;
        for (Listener<?> subscription : subscriptions) {
            try {
                subscription.deliver(event);
            } catch (RuntimeException e) {
                if (onError != null) {
                    onError.accept(event, e);
                } else if (first == null) {
                    first = e;
                }
            }
        }
        if (first != null) throw first;
    }

    // Una clase y no un record: dos suscripciones iguales se tienen que poder cancelar por separado.
    private static final class Listener<E extends DomainEvent> {
        private final Class<E> type;
        private final Consumer<? super E> subscriber;

        Listener(Class<E> type, Consumer<? super E> subscriber) {
            this.type = type;
            this.subscriber = subscriber;
        }

        void deliver(DomainEvent event) {
            if (type.isInstance(event)) subscriber.accept(type.cast(event));
        }
    }
}
