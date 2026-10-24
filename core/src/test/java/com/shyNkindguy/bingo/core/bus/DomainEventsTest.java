package com.shyNkindguy.bingo.core.bus;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainEventsTest {
    private static final CellCompleted CELL = new CellCompleted("rojo", "id-ana", "Ana", "item:dirt", "Tierra", 30);
    private static final GameEnded END = new GameEnded(600, false, Set.of("rojo"), List.of());

    @Test
    void cadaSuscriptorRecibeSoloSuTipoEnOrden() {
        DomainEvents events = new DomainEvents();
        List<String> seen = new ArrayList<>();
        events.subscribe(CellCompleted.class, event -> seen.add("casilla 1: " + event.taskId()));
        events.subscribe(GameEnded.class, event -> seen.add("fin: " + event.seconds()));
        events.subscribe(DomainEvent.class, event -> seen.add("cualquiera"));
        events.subscribe(CellCompleted.class, event -> seen.add("casilla 2"));

        events.publish(CELL);
        events.publish(END);

        assertEquals(List.of("casilla 1: item:dirt", "cualquiera", "casilla 2", "fin: 600", "cualquiera"), seen);
    }

    @Test
    void siUnoFallaLosDemasLoRecibenIgual() {
        List<RuntimeException> errors = new ArrayList<>();
        DomainEvents events = new DomainEvents((event, e) -> errors.add(e));
        List<String> seen = new ArrayList<>();
        RuntimeException boom = new IllegalStateException("se rompió");
        events.subscribe(CellCompleted.class, event -> { throw boom; });
        events.subscribe(CellCompleted.class, event -> seen.add("después"));

        events.publish(CELL);

        assertEquals(List.of("después"), seen);
        assertEquals(List.of(boom), errors);
    }

    @Test
    void sinManejadorElErrorSeRelanzaDespuesDeEntregarATodos() {
        DomainEvents events = new DomainEvents();
        List<String> seen = new ArrayList<>();
        RuntimeException boom = new IllegalStateException("se rompió");
        events.subscribe(GameEnded.class, event -> { throw boom; });
        events.subscribe(GameEnded.class, event -> seen.add("después"));

        assertSame(boom, assertThrows(IllegalStateException.class, () -> events.publish(END)));
        assertEquals(List.of("después"), seen);
    }

    @Test
    void ganoQuienEstaEnUnEquipoGanador() {
        GameEnded.Participant ana = new GameEnded.Participant("id-ana", "Ana", "rojo");
        GameEnded.Participant beto = new GameEnded.Participant("id-beto", "Beto", "azul");
        GameEnded end = new GameEnded(100, false, Set.of("rojo"), List.of(ana, beto));
        assertEquals(true, end.won(ana));
        assertEquals(false, end.won(beto));
    }
}
