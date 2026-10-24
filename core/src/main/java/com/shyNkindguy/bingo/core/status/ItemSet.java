package com.shyNkindguy.bingo.core.status;

import java.util.List;
import java.util.Objects;

// Un conjunto de sets.yml: piezas marcadas (un ítem de Minecraft con nombre cada una) y los estados que da
// llevar `needed` piezas distintas a la vez, en cualquier lugar del inventario.
public record ItemSet(String id, String name, List<Piece> pieces, int needed, List<PlayerStatus> statuses) {

    public record Piece(String material, String name) {
        public Piece {
            Objects.requireNonNull(material, "material");
            Objects.requireNonNull(name, "name");
        }
    }

    public ItemSet {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        pieces = List.copyOf(pieces);
        statuses = List.copyOf(statuses);
        if (needed < 1 || needed > pieces.size()) {
            throw new IllegalArgumentException("necesita fuera de rango: " + needed + " de " + pieces.size());
        }
    }
}
