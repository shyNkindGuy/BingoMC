package com.shyNkindguy.bingo.config;

import com.shyNkindguy.bingo.adapter.Keys;
import com.shyNkindguy.bingo.core.catalog.CardSpec;
import com.shyNkindguy.bingo.core.catalog.Catalog;
import com.shyNkindguy.bingo.core.catalog.CatalogException;
import com.shyNkindguy.bingo.core.catalog.CatalogLoader;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// El catálogo de tareas y cartillas que usa /bingo start, leído de plugins/AdaptiveBingo/ (cards.yml y
// tasks/*.yml). La primera vez copia ahí los que vienen en el .jar.
public class CatalogService {
    private final JavaPlugin plugin;
    private Catalog catalog;

    public CatalogService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Al arrancar. Si los archivos tienen un error, lo dice en la consola y juega con los que vienen en el
    // .jar hasta que un /bingo reload los lea bien.
    public void load() {
        Logger log = plugin.getLogger();
        Path folder = plugin.getDataFolder().toPath();
        try {
            for (String file : CatalogLoader.copyDefaults(folder)) log.info("Creado " + file);
        } catch (IOException e) {
            log.warning("No se pudieron copiar los YAML por defecto: " + e.getMessage());
        }

        try {
            catalog = CatalogLoader.fromDirectory(folder);
            summary(catalog).forEach(log::info);
        } catch (CatalogException e) {
            log.severe(e.getMessage());
            log.severe("Se usan las tareas que vienen en el plugin hasta que /bingo reload lea bien los archivos.");
            catalog = CatalogLoader.defaults();
        }
    }

    // /bingo reload. Si los archivos tienen un error lanza CatalogException y queda el catálogo anterior.
    public List<String> reload() {
        Catalog loaded = CatalogLoader.fromDirectory(plugin.getDataFolder().toPath());
        catalog = loaded;
        return summary(loaded);
    }

    public Catalog get() {
        return catalog;
    }

    // Si la tarea se puede hacer en esta versión de Minecraft: hoy, que su ítem exista.
    public static boolean isAvailable(Task task) {
        return !(task instanceof ItemTask item) || Keys.toMaterial(item.item()) != null;
    }

    // "3 listas, 43 tareas; cartillas: normal 5×5 (por defecto), rapida 3×3" y, si hay, las tareas que no
    // van a salir porque su ítem no existe en esta versión.
    static List<String> summary(Catalog catalog) {
        List<String> cards = new ArrayList<>();
        for (CardSpec card : catalog.cards().values()) {
            String isDefault = card.name().equals(catalog.defaultCard()) ? " (por defecto)" : "";
            cards.add(card.name() + " " + card.size() + "×" + card.size() + isDefault);
        }

        List<String> lines = new ArrayList<>();
        lines.add(catalog.lists().size() + " listas, " + catalog.tasks().size() + " tareas; cartillas: "
                + String.join(", ", cards));

        List<String> missing = new ArrayList<>();
        for (Task task : catalog.tasks()) {
            if (!isAvailable(task)) missing.add(task.id());
        }
        if (!missing.isEmpty()) {
            lines.add("No existen en esta versión y no van a salir: " + String.join(", ", missing));
        }
        return lines;
    }
}
