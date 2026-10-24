package com.shyNkindguy.bingo.core.card;

// Lo que pide una casilla. `item` es la clave de Minecraft del ítem ("minecraft:diamond"); el plugin la
// traduce a Material, el núcleo no conoce Bukkit.
public class BingoObjective {
    private final String id;
    private final String displayName;
    private final String item;

    public BingoObjective(String id, String displayName, String item) {
        this.id = id;
        this.displayName = displayName;
        this.item = item;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getItem() {
        return item;
    }
}
