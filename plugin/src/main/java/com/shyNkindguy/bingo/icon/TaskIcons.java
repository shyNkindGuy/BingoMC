package com.shyNkindguy.bingo.icon;

import com.shyNkindguy.bingo.adapter.Keys;
import com.shyNkindguy.bingo.core.task.AdvancementTask;
import com.shyNkindguy.bingo.core.task.KillTask;
import com.shyNkindguy.bingo.core.task.StatisticTask;
import com.shyNkindguy.bingo.core.task.Task;
import com.shyNkindguy.bingo.icon.presets.BadgeIcons;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;

import java.util.HashMap;
import java.util.Map;

// Qué se dibuja en la casilla de cada tarea: el ítem de su ícono (o el del logro) y la marca del tipo
// (matar, logro, estadística). Las de ítem no llevan marca.
public final class TaskIcons {
    private static final IconDefinition KILL = new IconDefinition("marca_matar", BadgeIcons.KILL, IconCategory.MISC);
    private static final IconDefinition ADVANCEMENT =
            new IconDefinition("marca_logro", BadgeIcons.ADVANCEMENT, IconCategory.MISC);
    private static final IconDefinition STATISTIC =
            new IconDefinition("marca_estadistica", BadgeIcons.STATISTIC, IconCategory.MISC);

    private static final Map<String, Material> ADVANCEMENT_ICONS = new HashMap<>();

    private TaskIcons() {
    }

    // El ítem a dibujar; null si no hay ninguno (se dibuja la marca en grande).
    public static Material material(Task task) {
        if (task.icon() != null) return Keys.toMaterial(task.icon());
        if (task instanceof AdvancementTask advancement) {
            return ADVANCEMENT_ICONS.computeIfAbsent(advancement.advancement(), TaskIcons::advancementIcon);
        }
        return null;
    }

    // La marca del tipo de tarea; null para las de ítem.
    public static IconDefinition badge(Task task) {
        return switch (task) {
            case KillTask kill -> KILL;
            case AdvancementTask advancement -> ADVANCEMENT;
            case StatisticTask statistic -> STATISTIC;
            default -> null;
        };
    }

    private static Material advancementIcon(String key) {
        Advancement advancement = Keys.toAdvancement(key);
        if (advancement == null || advancement.getDisplay() == null) return null;
        return advancement.getDisplay().icon().getType();
    }
}
