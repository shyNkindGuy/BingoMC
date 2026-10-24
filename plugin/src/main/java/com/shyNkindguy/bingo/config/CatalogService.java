package com.shyNkindguy.bingo.config;

import com.shyNkindguy.bingo.core.catalog.CardSpec;
import com.shyNkindguy.bingo.core.catalog.Catalog;
import com.shyNkindguy.bingo.core.catalog.CatalogException;
import com.shyNkindguy.bingo.core.catalog.CatalogLoader;
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
    private final TaskAvailability availability;
    private Catalog catalog;

    public CatalogService(JavaPlugin plugin, TaskAvailability availability) {
        this.plugin = plugin;
        this.availability = availability;
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
            summary(catalog, availability).forEach(log::info);
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
        return summary(loaded, availability);
    }

    public Catalog get() {
        return catalog;
    }

    public boolean isAvailable(Task task) {
        return availability.isAvailable(task);
    }

    // "3 listas, 59 tareas; cartillas: normal 5×5 (por defecto), rapida 3×3" y, si hay, las tareas que no
    // van a salir porque no se pueden hacer en esta versión, con el motivo.
    static List<String> summary(Catalog catalog, TaskAvailability availability) {
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
            String problem = availability.problem(task);
            if (problem != null) missing.add(task.id() + " (" + problem + ")");
        }
        if (!missing.isEmpty()) {
            lines.add("No se pueden hacer en esta versión y no van a salir: " + String.join(", ", missing));
        }
        return lines;
    }
}
