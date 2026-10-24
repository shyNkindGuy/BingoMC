package com.shyNkindguy.bingo.core.bus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// Bus de eventos sincrónico: quien se suscribe a un tipo recibe los eventos de ese tipo en el mismo hilo que
// los publica, en el orden en que se suscribió. Si un suscriptor falla, los demás lo reciben igual y el
// error va a `onError` (por defecto se relanza el primero después de entregarlo a todos).
public final class DomainEvents {
    private final List<Subscription<?>> subscriptions = new CopyOnWriteArrayList<>();
    private final BiConsumer<DomainEvent, RuntimeException> onError;

    public DomainEvents() {
        this(null);
    }

    public DomainEvents(BiConsumer<DomainEvent, RuntimeException> onError) {
        this.onError = onError;
    }

    public <E extends DomainEvent> void subscribe(Class<E> type, Consumer<? super E> subscriber) {
        subscriptions.add(new Subscription<>(type, subscriber));
    }

    public void publish(DomainEvent event) {
        RuntimeException first = null;
        for (Subscription<?> subscription : subscriptions) {
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

    private record Subscription<E extends DomainEvent>(Class<E> type, Consumer<? super E> subscriber) {
        void deliver(DomainEvent event) {
            if (type.isInstance(event)) subscriber.accept(type.cast(event));
        }
    }
}
