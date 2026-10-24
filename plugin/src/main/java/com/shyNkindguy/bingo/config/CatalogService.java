package com.shyNkindguy.bingo.config;

import com.shyNkindguy.bingo.core.catalog.CardSpec;
import com.shyNkindguy.bingo.core.catalog.Catalog;
import com.shyNkindguy.bingo.core.catalog.CatalogException;
import com.shyNkindguy.bingo.core.catalog.CatalogLoader;
import com.shyNkindguy.bingo.adapter.Keys;
import com.shyNkindguy.bingo.core.game.GameSettings;
import com.shyNkindguy.bingo.core.game.StartRules;
import com.shyNkindguy.bingo.core.task.Task;
import com.shyNkindguy.bingo.core.text.Messages;
import com.shyNkindguy.bingo.ui.Texts;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// El catálogo de tareas y cartillas (cards.yml y tasks/*.yml), la configuración de las partidas
// (config.yml) y los textos (messages.yml, que van a Texts), leídos de plugins/AdaptiveBingo/. La primera vez
// copia ahí los que vienen en el .jar.
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
            kitProblems(settings).forEach(log::warning);
        } catch (CatalogException e) {
            log.severe(e.getMessage());
            log.severe("Se usa la configuración por defecto hasta que /bingo reload lea bien config.yml.");
            settings = GameSettings.DEFAULT;
        }
        try {
            Texts.use(CatalogLoader.messagesFromDirectory(folder));
        } catch (CatalogException e) {
            log.severe(e.getMessage());
            log.severe("Se usan los textos por defecto hasta que /bingo reload lea bien messages.yml.");
            Texts.use(CatalogLoader.defaultMessages());
        }
    }

    // /bingo reload. Si algún archivo tiene un error lanza CatalogException y quedan todos los de antes (van
    // juntos: una cartilla de config.yml tiene que estar en cards.yml).
    public List<String> reload() {
        Path folder = plugin.getDataFolder().toPath();
        Catalog loadedCatalog = CatalogLoader.fromDirectory(folder);
        GameSettings loadedSettings = CatalogLoader.settingsFromDirectory(folder, loadedCatalog);
        Messages loadedMessages = CatalogLoader.messagesFromDirectory(folder);
        catalog = loadedCatalog;
        settings = loadedSettings;
        Texts.use(loadedMessages);
        List<String> lines = new ArrayList<>(summary(loadedCatalog, availability));
        lines.addAll(kitProblems(loadedSettings));
        return lines;
    }

    public Catalog get() {
        return catalog;
    }

    public GameSettings settings() {
        return settings;
    }

    // Los ítems del kit que no existen en esta versión (no se dan).
    static List<String> kitProblems(GameSettings settings) {
        List<String> problems = new ArrayList<>();
        for (StartRules.KitItem item : settings.start().kit()) {
            if (Keys.toMaterial(item.item()) == null) problems.add(Texts.get("recarga.kit", "item", item.item()));
        }
        return problems;
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
