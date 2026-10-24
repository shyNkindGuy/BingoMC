package com.shyNkindguy.bingo.items;

import com.shyNkindguy.bingo.core.catalog.CatalogLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecialItemsTest {

    @Test
    void laDescripcionDeUnaPiezaDiceQueDaElConjunto() {
        assertEquals(List.of("§7Conjunto Explorador: con 2 de 3 piezas, velocidad, visión nocturna"),
                SpecialItems.pieceLore(CatalogLoader.defaultSets().get(0)));
        assertEquals(List.of("§7Conjunto Minero: con 2 de 3 piezas, prisa minera II, visión nocturna"),
                SpecialItems.pieceLore(CatalogLoader.defaultSets().get(1)));
    }
}
