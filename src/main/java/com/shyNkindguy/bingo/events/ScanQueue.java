package com.shyNkindguy.bingo.events;

import java.util.HashSet;
import java.util.Set;

// Junta los pedidos de escaneo de inventario de un mismo tick: muchos eventos seguidos del mismo
// jugador (recoger varios ítems, clics rápidos) terminan en un solo escaneo.
public final class ScanQueue<K> {
    private final Set<K> pending = new HashSet<>();

    // true si no había un escaneo pendiente para esta clave: el que llama debe programarlo.
    public boolean request(K key) {
        return pending.add(key);
    }

    // Se llama al empezar el escaneo programado, para que el próximo evento vuelva a pedir uno.
    public void done(K key) {
        pending.remove(key);
    }
}
