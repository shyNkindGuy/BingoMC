package com.shyNkindguy.bingo.core.progress;

// Avance de una tarea: `current` de `target`. Inmutable y solo sube: tirar los ítems después no
// des-completa la casilla.
public record TaskProgress(int current, int target) {

    public TaskProgress {
        if (target < 1) throw new IllegalArgumentException("El objetivo tiene que ser al menos 1: " + target);
        if (current < 0 || current > target) {
            throw new IllegalArgumentException("Avance fuera de rango: " + current + "/" + target);
        }
    }

    public static TaskProgress start(int target) {
        return new TaskProgress(0, target);
    }

    public static TaskProgress done(int target) {
        return new TaskProgress(target, target);
    }

    // El avance después de haber llegado a `amount` (no baja, no pasa del objetivo).
    public TaskProgress reach(int amount) {
        int next = Math.min(target, Math.max(current, amount));
        return next == current ? this : new TaskProgress(next, target);
    }

    public boolean isComplete() {
        return current == target;
    }

    // Cuánto de `width` corresponde al avance (para dibujar una barra): 0 sin avance, `width` completa.
    public int scaledTo(int width) {
        return current * width / target;
    }
}
