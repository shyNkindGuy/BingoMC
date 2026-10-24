package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.status.ItemSet;
import com.shyNkindguy.bingo.core.status.PlayerStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.shyNkindguy.bingo.core.catalog.CatalogParser.checkKeys;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.error;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.key;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.list;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.map;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.positive;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.required;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.statuses;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.text;

// sets.yml: `conjuntos:` es una lista; cada conjunto tiene id, nombre, piezas (item + nombre), necesita (cuántas
// piezas distintas a la vez; si falta, todas) y estados. Todo se valida con "archivo, conjunto N: problema".
final class SetParser {
    private static final Pattern ID = Pattern.compile("[a-z0-9_]+");
    private static final Set<String> TOP_KEYS = Set.of("conjuntos");
    private static final Set<String> SET_KEYS = Set.of("id", "nombre", "piezas", "necesita", "estados");
    private static final Set<String> PIECE_KEYS = Set.of("item", "nombre");

    private SetParser() {
    }

    static List<ItemSet> parse(String file, Object root) {
        if (root == null) return List.of();
        Map<String, Object> top = map(root, file);
        checkKeys(top, TOP_KEYS, file);
        if (top.get("conjuntos") == null) return List.of();

        List<?> entries = list(top.get("conjuntos"), file + ": conjuntos");
        List<ItemSet> sets = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < entries.size(); i++) {
            String where = file + ", conjunto " + (i + 1);
            ItemSet set = set(map(entries.get(i), where), where);
            if (!ids.add(set.id())) throw error(where, "el id " + set.id() + " ya está en otro conjunto");
            sets.add(set);
        }
        return List.copyOf(sets);
    }

    private static ItemSet set(Map<String, Object> map, String where) {
        checkKeys(map, SET_KEYS, where);
        String id = text(required(map, "id", where), where + ": id");
        if (!ID.matcher(id).matches()) throw error(where + ": id", "solo minúsculas, números y _ (vino " + id + ")");
        String name = text(required(map, "nombre", where), where + ": nombre");

        List<?> rawPieces = list(required(map, "piezas", where), where + ": piezas");
        List<ItemSet.Piece> pieces = new ArrayList<>();
        for (int i = 0; i < rawPieces.size(); i++) {
            String pieceWhere = where + ", pieza " + (i + 1);
            Map<String, Object> piece = map(rawPieces.get(i), pieceWhere);
            checkKeys(piece, PIECE_KEYS, pieceWhere);
            pieces.add(new ItemSet.Piece(key(required(piece, "item", pieceWhere), pieceWhere + ": item"),
                    text(required(piece, "nombre", pieceWhere), pieceWhere + ": nombre")));
        }

        int needed = map.containsKey("necesita") ? positive(map.get("necesita"), where + ": necesita") : pieces.size();
        if (needed > pieces.size()) {
            throw error(where + ": necesita", "hay " + pieces.size() + " piezas y pide " + needed);
        }
        List<PlayerStatus> statuses = statuses(required(map, "estados", where), where + ": estados");
        if (statuses.isEmpty()) throw error(where + ": estados", "está vacía (un conjunto sin estados no hace nada)");
        return new ItemSet(id, name, pieces, needed, statuses);
    }
}
