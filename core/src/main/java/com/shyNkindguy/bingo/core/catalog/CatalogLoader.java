package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.game.GameSettings;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// Lee el catálogo de una carpeta (cards.yml + tasks/*.yml, en UTF-8) o el que viene adentro del .jar
// (recursos en adaptivebingo/defaults/), y la configuración de las partidas (config.yml). La carpeta del
// plugin se llena con esos la primera vez.
public final class CatalogLoader {

    public static final String CARDS_FILE = "cards.yml";
    public static final String CONFIG_FILE = "config.yml";
    public static final String TASKS_DIR = "tasks";
    static final List<String> DEFAULT_FILES =
            List.of(CONFIG_FILE, CARDS_FILE, "tasks/basicos.yml", "tasks/medios.yml", "tasks/dificiles.yml");
    private static final String DEFAULTS = "/adaptivebingo/defaults/";

    private CatalogLoader() {
    }

    public static Catalog fromDirectory(Path dir) {
        Path cards = dir.resolve(CARDS_FILE);
        if (!Files.isRegularFile(cards)) throw new CatalogException("Falta " + CARDS_FILE + " en " + dir);

        List<Path> taskFiles;
        try (Stream<Path> files = Files.list(dir.resolve(TASKS_DIR))) {
            taskFiles = files.filter(file -> file.getFileName().toString().endsWith(".yml"))
                    .filter(Files::isRegularFile)
                    .sorted()
                    .toList();
        } catch (NoSuchFileException e) {
            taskFiles = List.of();
        } catch (IOException e) {
            throw new CatalogException("No se pudo leer la carpeta " + TASKS_DIR + ": " + e.getMessage(), e);
        }

        List<TaskList> lists = new ArrayList<>();
        for (Path file : taskFiles) {
            String name = TASKS_DIR + "/" + file.getFileName();
            lists.add(CatalogParser.parseTaskList(name, read(name, () -> Files.newBufferedReader(file, StandardCharsets.UTF_8))));
        }
        return CatalogParser.parseCatalog(CARDS_FILE,
                read(CARDS_FILE, () -> Files.newBufferedReader(cards, StandardCharsets.UTF_8)), lists);
    }

    public static Catalog defaults() {
        List<TaskList> lists = new ArrayList<>();
        for (String file : DEFAULT_FILES) {
            if (isTaskFile(file)) lists.add(CatalogParser.parseTaskList(file, read(file, () -> openDefault(file))));
        }
        return CatalogParser.parseCatalog(CARDS_FILE, read(CARDS_FILE, () -> openDefault(CARDS_FILE)), lists);
    }

    // config.yml de la carpeta; si no está, la configuración por defecto.
    public static GameSettings settingsFromDirectory(Path dir, Catalog catalog) {
        Path config = dir.resolve(CONFIG_FILE);
        if (!Files.isRegularFile(config)) return GameSettings.DEFAULT;
        return CatalogParser.parseSettings(CONFIG_FILE,
                read(CONFIG_FILE, () -> Files.newBufferedReader(config, StandardCharsets.UTF_8)), catalog);
    }

    public static GameSettings defaultSettings(Catalog catalog) {
        return CatalogParser.parseSettings(CONFIG_FILE, read(CONFIG_FILE, () -> openDefault(CONFIG_FILE)), catalog);
    }

    // Copia los YAML por defecto que falten: config.yml y cards.yml si no están, y las listas solo si no existe la
    // carpeta tasks/ (así una lista borrada a propósito no vuelve). Devuelve los archivos copiados.
    public static List<String> copyDefaults(Path dir) throws IOException {
        boolean tasksMissing = !Files.exists(dir.resolve(TASKS_DIR));
        List<String> copied = new ArrayList<>();
        for (String file : DEFAULT_FILES) {
            Path target = dir.resolve(file);
            boolean missing = isTaskFile(file) ? tasksMissing : !Files.exists(target);
            if (!missing) continue;

            Files.createDirectories(target.getParent());
            try (InputStream in = defaultStream(file)) {
                Files.copy(in, target);
            }
            copied.add(file);
        }
        return copied;
    }

    private static boolean isTaskFile(String file) {
        return file.startsWith(TASKS_DIR + "/");
    }

    private static Reader openDefault(String file) throws IOException {
        return new InputStreamReader(defaultStream(file), StandardCharsets.UTF_8);
    }

    private static InputStream defaultStream(String file) throws IOException {
        InputStream in = CatalogLoader.class.getResourceAsStream(DEFAULTS + file);
        if (in == null) throw new IOException("falta " + DEFAULTS + file + " adentro del .jar");
        return in;
    }

    private interface Source {
        Reader open() throws IOException;
    }

    private static Object read(String name, Source source) {
        try (Reader reader = source.open()) {
            return new Yaml(new SafeConstructor(new LoaderOptions())).load(reader);
        } catch (YAMLException e) {
            if (e.getCause() instanceof CharacterCodingException) throw notUtf8(name, e);
            throw new CatalogException(name + ": YAML mal escrito: " + e.getMessage(), e);
        } catch (CharacterCodingException e) {
            throw notUtf8(name, e);
        } catch (IOException e) {
            throw new CatalogException(name + ": no se pudo leer: " + e.getMessage(), e);
        }
    }

    private static CatalogException notUtf8(String name, Exception cause) {
        return new CatalogException(name + ": no está guardado en UTF-8 (guárdalo como UTF-8 y vuelve a cargar)", cause);
    }
}
