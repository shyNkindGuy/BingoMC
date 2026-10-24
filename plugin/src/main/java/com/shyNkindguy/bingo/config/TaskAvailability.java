package com.shyNkindguy.bingo.config;

import com.shyNkindguy.bingo.adapter.Keys;
import com.shyNkindguy.bingo.core.task.AdvancementTask;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.KillTask;
import com.shyNkindguy.bingo.core.task.StatisticTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

import java.util.function.Predicate;

// Si una tarea se puede hacer en esta versión de Minecraft y, si no, por qué. Las que no se pueden no salen
// en ninguna cartilla. Lo que solo se sabe con el servidor andando (si un logro existe, si un Material es
// bloque o ítem: Material.isBlock() lee el registro del servidor) se pasa de afuera, así los tests no
// necesitan uno.
public class TaskAvailability {
    private final Predicate<String> advancementExists;
    private final Predicate<Material> isBlock;
    private final Predicate<Material> isItem;

    public TaskAvailability(Predicate<String> advancementExists, Predicate<Material> isBlock,
                            Predicate<Material> isItem) {
        this.advancementExists = advancementExists;
        this.isBlock = isBlock;
        this.isItem = isItem;
    }

    public static TaskAvailability ofServer() {
        return new TaskAvailability(key -> Keys.toAdvancement(key) != null, Material::isBlock, Material::isItem);
    }

    public boolean isAvailable(Task task) {
        return problem(task) == null;
    }

    // null si se puede hacer; si no, el motivo ("el mob no existe").
    public String problem(Task task) {
        return switch (task) {
            case ItemTask item -> Keys.toMaterial(item.item()) == null ? "el ítem no existe" : null;
            case KillTask kill -> killProblem(kill);
            case AdvancementTask advancement ->
                    advancementExists.test(advancement.advancement()) ? null : "el logro no existe";
            case StatisticTask statistic -> statisticProblem(statistic);
            default -> null;
        };
    }

    private static String killProblem(KillTask task) {
        EntityType type = Keys.toEntityType(task.entity());
        if (type == null) return "el mob no existe";
        return type.isAlive() ? null : "no es un mob";
    }

    private String statisticProblem(StatisticTask task) {
        Statistic statistic = Keys.toStatistic(task.statistic());
        if (statistic == null) return "la estadística no existe";

        String subject = task.subject();
        if (statistic.getType() == Statistic.Type.UNTYPED) {
            return subject == null ? null : "esta estadística no lleva de";
        }
        if (subject == null) return "falta de (" + subjectKind(statistic.getType()) + ")";

        boolean valid = switch (statistic.getType()) {
            case BLOCK -> {
                Material material = Keys.toMaterial(subject);
                yield material != null && isBlock.test(material);
            }
            case ITEM -> {
                Material material = Keys.toMaterial(subject);
                yield material != null && isItem.test(material);
            }
            case ENTITY -> Keys.toEntityType(subject) != null;
            default -> false;
        };
        return valid ? null : "de tiene que ser " + subjectKind(statistic.getType());
    }

    private static String subjectKind(Statistic.Type type) {
        return switch (type) {
            case BLOCK -> "un bloque";
            case ITEM -> "un ítem";
            case ENTITY -> "un mob";
            default -> "nada";
        };
    }
}
