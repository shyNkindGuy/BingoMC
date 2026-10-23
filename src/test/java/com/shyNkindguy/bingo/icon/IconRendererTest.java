package com.shyNkindguy.bingo.icon;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class IconRendererTest {

    // Lienzo de prueba: guarda cada píxel pintado en "x,y".
    private final Map<String, Byte> lienzo = new HashMap<>();
    private final IconRenderer.PixelSink sink = (x, y, color) -> lienzo.put(x + "," + y, color);

    private static IconDefinition icono(byte[][] pixels) {
        return new IconDefinition("prueba", pixels, IconCategory.MISC);
    }

    private static byte[][] lleno(int lado, byte color) {
        byte[][] pixels = new byte[lado][lado];
        for (byte[] fila : pixels) java.util.Arrays.fill(fila, color);
        return pixels;
    }

    @Test
    void escalaQueEntraEnLaCasilla() {
        assertEquals(2, IconRenderer.fitScale(18, 8, 8));
        assertEquals(3, IconRenderer.fitScale(24, 8, 4));
        assertEquals(1, IconRenderer.fitScale(9, 8, 8));
    }

    @Test
    void laEscalaNuncaEsCero() {
        assertEquals(1, IconRenderer.fitScale(4, 8, 8));
    }

    @Test
    void cadaPixelSeVuelveUnCuadradoYLoTransparenteNoSePinta() {
        IconRenderer.drawScaled(sink, 10, 20, icono(new byte[][]{{1, 0}, {0, 2}}), 2, false);

        assertEquals(8, lienzo.size());
        for (String p : new String[]{"10,20", "11,20", "10,21", "11,21"}) assertEquals((byte) 1, lienzo.get(p));
        for (String p : new String[]{"12,22", "13,22", "12,23", "13,23"}) assertEquals((byte) 2, lienzo.get(p));
        assertFalse(lienzo.containsKey("12,20"));
    }

    @Test
    void enLaCasillaDelMapaElIconoDe8SaleAlDobleYCentrado() {
        // Casilla de 22 px del mapa: el ícono de 8×8 sale a 16×16 con 3 px de borde a cada lado.
        IconRenderer.drawInCell(sink, 100, 50, 22, icono(lleno(8, (byte) 5)), false);

        assertEquals(16 * 16, lienzo.size());
        assertEquals((byte) 5, lienzo.get("103,53"));
        assertEquals((byte) 5, lienzo.get("118,68"));
        assertFalse(lienzo.containsKey("102,53"));
        assertFalse(lienzo.containsKey("119,68"));
    }
}
