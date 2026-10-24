package com.shyNkindguy.bingo.core.status;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Qué estados tiene un jugador ahora: los de la partida (config.yml `estados`) más los de cada conjunto del que
// lleve suficientes piezas distintas. Si dos dan el mismo efecto, queda el nivel más alto.
public final class StatusResolver {

    public record Result(List<PlayerStatus> statuses, List<ItemSet> activeSets) {
        public Result {
            statuses = List.copyOf(statuses);
            activeSets = List.copyOf(activeSets);
        }
    }

    private StatusResolver() {
    }

    // `carried`: id del conjunto → qué piezas (por posición en el conjunto) lleva.
    public static Result resolve(List<PlayerStatus> base, List<ItemSet> sets, Map<String, Set<Integer>> carried) {
        Map<String, PlayerStatus> byEffect = new LinkedHashMap<>();
        base.forEach(status -> keepHighest(byEffect, status));
        List<ItemSet> active = new ArrayList<>();
        for (ItemSet set : sets) {
            if (carried.getOrDefault(set.id(), Set.of()).size() < set.needed()) continue;
            active.add(set);
            set.statuses().forEach(status -> keepHighest(byEffect, status));
        }
        return new Result(new ArrayList<>(byEffect.values()), active);
    }

    private static void keepHighest(Map<String, PlayerStatus> byEffect, PlayerStatus status) {
        byEffect.merge(status.effect(), status, (old, now) -> now.level() > old.level() ? now : old);
    }
}
