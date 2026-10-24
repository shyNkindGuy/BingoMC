package com.shyNkindguy.bingo.core.progress;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticBaselineTest {

    @Test
    void cuentaDesdeElPrimerValorVisto() {
        StatisticBaseline<String> base = new StatisticBaseline<>();

        assertEquals(0, base.sinceStart("ana", "minecraft:jump", 5000));
        assertEquals(30, base.sinceStart("ana", "minecraft:jump", 5030));
        assertEquals(0, base.sinceStart("ana", "minecraft:deaths", 7));
    }

    @Test
    void cadaJugadorTieneSuPropioInicio() {
        StatisticBaseline<String> base = new StatisticBaseline<>();
        base.sinceStart("ana", "minecraft:jump", 100);
        base.sinceStart("beto", "minecraft:jump", 900);

        assertEquals(10, base.sinceStart("ana", "minecraft:jump", 110));
        assertEquals(1, base.sinceStart("beto", "minecraft:jump", 901));
    }

    @Test
    void nuncaDaNegativoYSeReiniciaEntrePartidas() {
        StatisticBaseline<String> base = new StatisticBaseline<>();
        base.sinceStart("ana", "minecraft:jump", 100);
        assertEquals(0, base.sinceStart("ana", "minecraft:jump", 50));

        base.clear();
        assertEquals(0, base.sinceStart("ana", "minecraft:jump", 50));
        assertEquals(5, base.sinceStart("ana", "minecraft:jump", 55));
    }
}
