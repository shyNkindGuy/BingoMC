package com.shyNkindguy.bingo.core.condition;

import com.shyNkindguy.bingo.core.event.EventContext;

// Sin haber muerto desde que empezó la partida.
public record NoDeath() implements Condition {

    @Override
    public boolean test(EventContext context) {
        return context.deaths() == 0;
    }

    @Override
    public String description() {
        return "sin morir";
    }

    @Override
    public String key() {
        return "sin_morir";
    }
}
