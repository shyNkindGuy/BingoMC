package com.shyNkindguy.bingo.core.condition;

import com.shyNkindguy.bingo.core.event.EventContext;

// Más abajo que la altura `y` (y < `y`).
public record BelowY(int y) implements Condition {

    @Override
    public boolean test(EventContext context) {
        return context.y() != null && context.y() < y;
    }

    @Override
    public String description() {
        return "debajo de Y " + y;
    }

    @Override
    public String key() {
        return "y<" + y;
    }
}
