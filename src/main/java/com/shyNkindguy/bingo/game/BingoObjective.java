package com.shyNkindguy.bingo.game;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BingoObjective {
    private final String id;
    private final String displayName;
    private final Material material;

    public BingoObjective(String id, String displayName, Material material) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }
}
