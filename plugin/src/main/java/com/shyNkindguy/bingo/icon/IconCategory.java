package com.shyNkindguy.bingo.icon;

import com.shyNkindguy.bingo.icon.presets.BlockIcons;
import com.shyNkindguy.bingo.icon.presets.FluidIcons;
import com.shyNkindguy.bingo.icon.presets.FoodIcons;
import com.shyNkindguy.bingo.icon.presets.TooLIcons;

public enum IconCategory {
    TOOL(TooLIcons.GENERIC_TOOL),
    WEAPON(TooLIcons.GENERIC_WEAPON),
    BLOCK(BlockIcons.GENERIC_BLOCK),
    FOOD(FoodIcons.GENERIC_FOOD),
    FLUID(FluidIcons.BUCKET_EMPTY),
    ORE(BlockIcons.GENERIC_ORE),
    PLANT(BlockIcons.SAPLING),
    DYE(FoodIcons.BERRY), // representativo
    MISC(BlockIcons.GENERIC_BLOCK);

    private final byte[][] fallbackIcon;

    IconCategory(byte[][] fallback) {
        this.fallbackIcon = fallback;
    }

    public byte[][] getFallback() {
        return fallbackIcon;
    }
}
