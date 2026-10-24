package com.shyNkindguy.bingo.config;

import com.shyNkindguy.bingo.core.catalog.CardSpec;
import com.shyNkindguy.bingo.core.catalog.Catalog;
import com.shyNkindguy.bingo.core.catalog.CatalogException;
import com.shyNkindguy.bingo.core.catalog.CatalogLoader;
import com.shyNkindguy.bingo.core.game.GameSettings;
import com.shyNkindguy.bingo.core.task.Task;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// El catálogo de tareas y cartillas (cards.yml y tasks/*.yml) y la configuración de las partidas
// (config.yml) que usa /bingo start, leídos de plugins/AdaptiveBingo/. La primera vez copia ahí los que
// vienen en el .jar.
public class CatalogService {
    private final JavaPlugin plugin;
    private final TaskAvailability availability;
    private Catalog catalog;
    private GameSettings settings;

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
        try {
            settings = CatalogLoader.settingsFromDirectory(folder, catalog);
        } catch (CatalogException e) {
            log.severe(e.getMessage());
            log.severe("Se usa la configuración por defecto hasta que /bingo reload lea bien config.yml.");
            settings = GameSettings.DEFAULT;
        }
    }

    // /bingo reload. Si los archivos tienen un error lanza CatalogException y quedan los de antes (las
    // tareas y la configuración van juntas: una cartilla de config.yml tiene que estar en cards.yml).
    public List<String> reload() {
        Path folder = plugin.getDataFolder().toPath();
        Catalog loadedCatalog = CatalogLoader.fromDirectory(folder);
        GameSettings loadedSettings = CatalogLoader.settingsFromDirectory(folder, loadedCatalog);
        catalog = loadedCatalog;
        settings = loadedSettings;
        return summary(loadedCatalog, availability);
    }

    public Catalog get() {
        return catalog;
    }

    public GameSettings settings() {
        return settings;
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
