package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.core.catalog.CatalogLoader;
import com.shyNkindguy.bingo.core.text.Messages;

// Los textos del juego (messages.yml) a mano desde cualquier lado: Texts.get("partida.ganador", "ganador",
// nombre). Arranca con los del .jar; CatalogService pone los del servidor al cargar y en /bingo reload.
public final class Texts {
    private static Messages messages = CatalogLoader.defaultMessages();

    private Texts() {
    }

    public static void use(Messages loaded) {
        messages = loaded;
    }

    public static String get(String key, Object... args) {
        return messages.get(key, args);
    }
}
