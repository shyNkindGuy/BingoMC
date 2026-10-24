package com.shyNkindguy.bingo.core.condition;

import com.shyNkindguy.bingo.core.event.EventContext;

// De día o de noche según la hora del mundo: de noche es de 13000 a 22999 (como en Minecraft, cuando
// aparecen los monstruos), de día el resto.
public record TimeOfDay(boolean night) implements Condition {

    public static final long NIGHT_START = 13000;
    public static final long NIGHT_END = 23000;

    public static boolean isNight(long timeOfDay) {
        long time = Math.floorMod(timeOfDay, 24000L);
        return time >= NIGHT_START && time < NIGHT_END;
    }

    @Override
    public boolean test(EventContext context) {
        return context.timeOfDay() != null && isNight(context.timeOfDay()) == night;
    }

    @Override
    public String description() {
        return night ? "de noche" : "de día";
    }

    @Override
    public String key() {
        return night ? "hora=noche" : "hora=dia";
    }
}
