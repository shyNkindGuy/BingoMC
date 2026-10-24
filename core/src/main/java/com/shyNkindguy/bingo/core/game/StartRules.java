package com.shyNkindguy.bingo.core.game;

import com.shyNkindguy.bingo.core.status.PlayerStatus;

import java.util.List;

// Cómo empieza la partida (config.yml): a qué distancia del spawn se reparte a los equipos (0 = no se
// teletransporta), el diámetro del borde del mundo (0 = sin borde), si se vacía el inventario, el kit que
// recibe cada jugador al "¡YA!" y los estados que tienen todos desde ahí hasta el final (desde la 3.4).
public record StartRules(int scatterRadius, int borderSize, boolean clearInventory, List<KitItem> kit,
                         List<PlayerStatus> statuses) {

    public static final int MAX_SCATTER = 100_000;
    public static final int MIN_BORDER = 16;
    public static final int MAX_BORDER = 60_000_000;
    public static final StartRules DEFAULT = new StartRules(0, 0, false, List.of(), List.of());

    public record KitItem(String item, int amount) {
        public KitItem {
            if (amount < 1) throw new IllegalArgumentException("La cantidad tiene que ser al menos 1: " + amount);
        }
    }

    public StartRules {
        kit = List.copyOf(kit);
        statuses = List.copyOf(statuses);
        if (scatterRadius < 0 || scatterRadius > MAX_SCATTER) {
            throw new IllegalArgumentException("Dispersión fuera de rango: " + scatterRadius);
        }
        if (borderSize != 0 && (borderSize < MIN_BORDER || borderSize > MAX_BORDER)) {
            throw new IllegalArgumentException("Borde fuera de rango: " + borderSize);
        }
        if (borderSize > 0 && scatterRadius * 2 >= borderSize) {
            throw new IllegalArgumentException("Con borde " + borderSize + " la dispersión tiene que ser menor que "
                    + borderSize / 2);
        }
    }
}
