package com.shyNkindguy.bingo.core.status;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusTest {
    private static final PlayerStatus VELOCIDAD = PlayerStatus.parse("velocidad");
    private static final PlayerStatus VISION = PlayerStatus.parse("vision_nocturna");

    private static ItemSet set(String id, int needed, PlayerStatus... statuses) {
        return new ItemSet(id, id, List.of(new ItemSet.Piece("minecraft:stick", "a"), new ItemSet.Piece("minecraft:bowl", "b"),
                new ItemSet.Piece("minecraft:apple", "c")), needed, List.of(statuses));
    }

    @Test
    void leeNombresConNivelTildesYEspacios() {
        assertEquals(new PlayerStatus("velocidad", "minecraft:speed", 1), VELOCIDAD);
        assertEquals(new PlayerStatus("prisa", "minecraft:haste", 2), PlayerStatus.parse("prisa 2"));
        assertEquals(new PlayerStatus("vision_nocturna", "minecraft:night_vision", 1), PlayerStatus.parse("Visión nocturna"));
        assertEquals("prisa minera II", PlayerStatus.parse("prisa 2").description());
        assertEquals("visión nocturna", VISION.description());
        assertTrue(assertThrows(IllegalArgumentException.class, () -> PlayerStatus.parse("volar")).getMessage()
                .startsWith("\"volar\" no es un estado (se puede usar: velocidad, vision_nocturna, sin_hambre"));
        assertEquals("el nivel tiene que ser de 1 a 5 (vino salto 9)",
                assertThrows(IllegalArgumentException.class, () -> PlayerStatus.parse("salto 9")).getMessage());
    }

    @Test
    void sumaLosDeLaPartidaYLosDeLosConjuntosConPiezasSuficientes() {
        ItemSet explorador = set("explorador", 2, VELOCIDAD, VISION);
        ItemSet minero = set("minero", 3, PlayerStatus.parse("prisa 2"));
        StatusResolver.Result result = StatusResolver.resolve(List.of(PlayerStatus.parse("sin_hambre")),
                List.of(explorador, minero), Map.of("explorador", Set.of(0, 2), "minero", Set.of(0, 1)));

        assertEquals(List.of(PlayerStatus.parse("sin_hambre"), VELOCIDAD, VISION), result.statuses());
        assertEquals(List.of(explorador), result.activeSets());
    }

    @Test
    void elMismoEfectoQuedaConElNivelMasAlto() {
        ItemSet rapido = set("rapido", 1, PlayerStatus.parse("velocidad 3"));
        StatusResolver.Result result = StatusResolver.resolve(List.of(VELOCIDAD), List.of(rapido), Map.of("rapido", Set.of(1)));
        assertEquals(List.of(PlayerStatus.parse("velocidad 3")), result.statuses());

        assertEquals(List.of(VELOCIDAD), StatusResolver.resolve(List.of(VELOCIDAD), List.of(rapido), Map.of()).statuses());
    }
}
