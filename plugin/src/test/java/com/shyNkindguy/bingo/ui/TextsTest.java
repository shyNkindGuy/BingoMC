package com.shyNkindguy.bingo.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

// Cada clave que usa el plugin tiene que estar en el messages.yml por defecto: si no, sale "[clave]".
class TextsTest {

    private static final String[] CLAVES = {
            "comando.uso", "comando.sin_permiso_iniciar", "comando.sin_permiso_detener", "comando.sin_permiso_recargar",
            "comando.sin_permiso_equipos", "comando.sin_permiso_pausar", "comando.solo_jugador",
            "pausa.titulo", "pausa.sigue_titulo", "pausa.subtitulo", "pausa.pausada", "pausa.sigue", "pausa.no_se_puede",
            "barra.pausa", "marcador.estado_pausa", "recarga.kit",
            "resumen.titulo", "resumen.equipo", "resumen.casilla", "resumen.mvp", "fin.aviso_volver", "fin.aviso_limpiar",
            "inicio.ya_en_curso", "inicio.sin_jugadores", "inicio.cartilla_no_existe", "inicio.error",
            "inicio.modo_imposible", "inicio.no_se_pudo", "inicio.anuncio", "inicio.detalle", "inicio.minutos",
            "inicio.sin_limite", "inicio.sin_pvp",
            "cuenta.titulo", "cuenta.subtitulo", "cuenta.ya_titulo", "cuenta.ya_subtitulo", "cuenta.a_jugar",
            "partida.bingo", "partida.ganador", "partida.tiempo", "partida.detenida", "partida.no_hay", "partida.entraste",
            "puntos.tiempo_agotado", "puntos.sin_libres", "puntos.nadie", "puntos.ganador", "puntos.empate", "puntos.casillas",
            "progreso.completado", "progreso.por", "progreso.titulo", "progreso.subtitulo", "progreso.anuncio",
            "progreso.lockout", "progreso.logros_reiniciados",
            "recarga.ok", "recarga.linea", "recarga.partida_en_curso", "recarga.error", "recarga.sigue",
            "mapa.nombre", "mapa.dado", "chat.sin_equipo", "chat.equipo",
            "equipos.uso", "equipos.bloqueados", "equipos.uso_unir", "equipos.color_no_existe", "equipos.unido",
            "equipos.salio", "equipos.sin_equipo", "equipos.uso_random", "equipos.al_azar", "equipos.ninguno",
            "equipos.linea", "equipos.desconectado",
            "marcador.titulo", "marcador.equipo", "marcador.solo", "marcador.sin_equipo", "marcador.casillas",
            "marcador.gana", "marcador.fila_equipo", "marcador.estado_cuenta", "marcador.estado_juego",
            "barra.cuenta", "barra.restante", "barra.sin_limite",
            "cartilla.titulo", "cartilla.no_jugando", "cartilla.progreso", "cartilla.completada", "cartilla.bloqueada",
            "cartilla.condiciones", "cartilla.tipo_item", "cartilla.tipo_matar", "cartilla.tipo_logro",
            "cartilla.tipo_estadistica", "cartilla.tipo_alguno", "cartilla.tipo_todos", "cartilla.item_de",
    };

    @Test
    void todasLasClavesQueUsaElPluginExisten() {
        for (String clave : CLAVES) assertFalse(Texts.get(clave).startsWith("["), "Falta en messages.yml: " + clave);
    }

    @Test
    void elFormatoDelChatDeEquipoConservaLosHuecosDeBukkit() {
        assertEquals("§c[Rojo] §r%1$s§7: §f%2$s", Texts.get("chat.equipo", "color", "§c", "equipo", "Rojo"));
    }
}
