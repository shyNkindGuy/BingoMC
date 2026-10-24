# AdaptiveBingo (BingoMC)

Plugin de **bingo para Minecraft (Paper)**, proyecto personal de shyNkindguy. Cada jugador (más
adelante, cada equipo) recibe una cartilla N×N de tareas dibujada en un mapa en la mano izquierda; gana
quien complete una línea (y más adelante, otros modos). A futuro: capa de servidor con logros,
estadísticas, ítems especiales, conjuntos y estados. Diseño en `docs/ARQUITECTURA.md`, diagnóstico y
viabilidad en `docs/ANALISIS.md`, orden de trabajo en `docs/PLAN.md`.

## Stack
- **Java 21, Paper API 1.21.11** (desde la tarea 0.2), Maven, un módulo (`pom.xml`). Decidido pasar a dos
  módulos `core` (sin Bukkit) + `plugin` (tarea 1.1). La versión del plugin sale solo del `pom` (el
  `plugin.yml` usa `${project.version}` con resource filtering; el CI revisa que quede reemplazada).
- `plugin.yml` clásico de Bukkit (no `paper-plugin.yml`). Main: `com.shyNkindguy.bingo.AdaptiveBingo`.
- Paquete `com.shyNkindguy.bingo`: `commands/` (`/bingo start|stop|map`), `events/` (`ObjectiveListener`),
  `game/` (partida, cartilla, objetivos, victoria), `icon/` (registro de íconos nuevo), `ui/` (mapa,
  scoreboard, paleta). Íconos: un solo sistema, `icon/IconRegistry` (por `Material`, con fallback por
  categoría) + `icon/IconRenderer` (escala entera que entra en la casilla, centrado; dibuja sobre un
  `PixelSink` para poder testearlo).

## Cómo se juega hoy
`/bingo start` (permiso `bingo.admin`, ops por defecto; también desde la consola) crea una cartilla 5×5
por jugador conectado con 25 objetivos sorteados **una vez por partida** de los 43 de `ObjectivePool`
(nombre del ítem + nombre en español; el id se deriva del ítem), los mismos para todos en distinto orden;
da el mapa y el scoreboard. El mapa usa un renderer contextual (cada jugador ve su cartilla) que solo
redibuja tras `invalidate()`. Una casilla se completa cuando el ítem está **en el inventario del jugador**: recoger, clic,
arrastrar, cerrar el inventario, craftear, horno o balde piden revisar el inventario un tick después
(`ObjectiveListener.requestScan`; `ScanQueue` junta los pedidos del mismo tick), nunca se mira el ítem
del evento. La cartilla es un mapa marcado con PDC `adaptivebingo:cartilla` (`BingoMapManager.isBingoMap`):
no pisa la mano izquierda (lo que había pasa al inventario o se suelta), no se duplica, no cae al morir y
no se puede tirar. Gana la primera línea (`WinConditionChecker`); a los 20 min (`MAX_TIME`), el que tenga más casillas o empate
entre los que más tengan (`TimeLimitWinners`). `/bingo stop` la detiene. Toda partida termina por
`BingoGame.finish()`: cancela el timer, corre los `onEnd` (scoreboard principal de vuelta y mapas
quitados del inventario) y borra las cartillas. `/bingo map` vuelve a dar el mapa. Bugs conocidos:
tabla de `docs/ANALISIS.md` §3.

## Convenciones
- Commits, mensajes del juego y documentación **en español**. Java (no migrar a Kotlin sin pedirlo).
- Lógica nueva **sin Bukkit** siempre que se pueda (clases puras con tests JUnit 5 en `src/test`); los
  listeners solo traducen eventos de Paper. Claves de texto (`"minecraft:diamond"`) en la lógica nueva.
- Nada de objetivos ni textos fijos nuevos en el código: van a YAML (desde la tarea 1.3).
- No usar `_` como identificador (es palabra reservada desde Java 9).
- No copiar código de BingoReloaded ni minecraft-bingo (GPL-3.0); sí sus ideas.

## Comandos
```bash
mvn -B verify          # compila, corre tests y deja target/AdaptiveBingo-<versión>.jar
```
Para probar: copiar el `.jar` (o el artefacto `adaptive-bingo-jar` del CI) a `plugins/` de un servidor
Paper 1.21.11 (Java 21) y reiniciar.

**El contenedor en la nube de Claude no llega a `repo.papermc.io`** (y Maven Central a veces responde
429): ahí `mvn verify` no puede bajar `paper-api`. La validación real es `.github/workflows/build.yml`
en cada PR. Antes de hacer push, las clases puras se prueban con `javac` + `junit-platform-console-
standalone` contra stubs mínimos de Bukkit en el scratchpad; desde la tarea 1.1, `mvn -pl core verify`.

## Reglas de trabajo para Claude (modo autónomo)
1. Una tarea de `docs/PLAN.md` = un PR desde `claude/compassionate-davinci-y5x96q` contra `master`
   (en borrador mientras se trabaja). Después de cada merge, la rama se reinicia desde `origin/master`.
2. **Un solo PR abierto a la vez.** Si hay uno: llevarlo a verde (CI, conflictos, comentarios). Si el
   CI está verde en el último commit, sin conflictos y sin comentarios pendientes: marcarlo listo y
   **fusionarlo** (merge commit; decisión del dueño, 2026-10-23) y seguir con la próxima tarea.
3. Nunca fusionar con CI rojo o sin correr; nunca hacer push directo a `master`; nunca saltar, desactivar
   ni borrar tests para poner verde el CI.
4. Al terminar una tarea: marcarla `[x]` en `docs/PLAN.md` en el mismo PR, actualizar este archivo y la
   sección "Dónde quedó" de `.claude/skills/bingomc-contexto/SKILL.md` si cambió algo, y dejar en el
   PR "Cómo probarlo en el servidor" con pasos concretos.
5. Si un PR se cerró sin fusionar, no repetir esa tarea: avisar al dueño y esperar.
6. La **Fase 4** (servidor) la decide el dueño: no empezarla; avisar una sola vez.
