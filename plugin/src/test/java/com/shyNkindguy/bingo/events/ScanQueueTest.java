package com.shyNkindguy.bingo.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScanQueueTest {

    @Test
    void elPrimerPedidoProgramaUnEscaneo() {
        ScanQueue<String> cola = new ScanQueue<>();
        assertTrue(cola.request("Ana"));
    }

    @Test
    void pedidosRepetidosEnElMismoTickNoProgramanOtro() {
        ScanQueue<String> cola = new ScanQueue<>();
        cola.request("Ana");
        assertFalse(cola.request("Ana"));
        assertFalse(cola.request("Ana"));
    }

    @Test
    void cadaJugadorTieneSuPropioEscaneo() {
        ScanQueue<String> cola = new ScanQueue<>();
        cola.request("Ana");
        assertTrue(cola.request("Beto"));
    }

    @Test
    void despuesDelEscaneoSePuedePedirOtro() {
        ScanQueue<String> cola = new ScanQueue<>();
        cola.request("Ana");
        cola.done("Ana");
        assertTrue(cola.request("Ana"));
    }
}
