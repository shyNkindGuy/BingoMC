# AdaptiveBingo (BingoMC)

Plugin de **bingo para Minecraft (Paper)**, proyecto personal de shyNkindguy. Cada jugador (más
adelante, cada equipo) recibe una cartilla N×N de tareas dibujada en un mapa en la mano izquierda; gana
quien complete una línea (y más adelante, otros modos). A futuro: capa de servidor con logros,
estadísticas, ítems especiales, conjuntos y estados. Diseño en `docs/ARQUITECTURA.md`, diagnóstico y
viabilidad en `docs/ANALISIS.md`, orden de trabajo en `docs/PLAN.md`.

## Stack
- **Java 17, Paper API 1.20.4**, Maven, un módulo (`pom.xml`). Decidido migrar a **Paper 1.21.11 +
  Java 21** (tarea 0.2) y luego a dos módulos `core` (sin Bukkit) + `plugin` (tarea 1.1).
- `plugin.yml` clásico de Bukkit (no `paper-plugin.yml`). Main: `com.shyNkindguy.bingo.AdaptiveBingo`.
- Paquete `com.shyNkindguy.bingo`: `commands/` (`/bingo start|map`), `events/` (`ObjectiveListener`),
  `game/` (partida, cartilla, objetivos, victoria), `icon/` (registro de íconos nuevo), `ui/` (mapa,
  scoreboard, paleta). Dos sistemas de íconos conviven hasta la tarea 0.5: el viejo
  `game/MapIconRegistry` + `IconPresets` (el que usa el mapa) y el nuevo `icon/IconRegistry`.

## Cómo se juega hoy
`/bingo start` crea una cartilla 5×5 por jugador conectado (los 25 objetivos de `ObjectiveFactory` en
distinto orden), da el mapa y el scoreboard. Una casilla se completa al tener el ítem (recoger,
craftear, horno, balde, clic). Gana la primera línea (`WinConditionChecker`); a los 20 min, el que tenga
más casillas. `/bingo map` vuelve a dar el mapa. Bugs conocidos: tabla de `docs/ANALISIS.md` §3.

## Convenciones
- Commits, mensajes del juego y documentación **en español**. Java (no migrar a Kotlin sin pedirlo).
- Lógica nueva **sin Bukkit** siempre que se pueda (clases puras con tests JUnit 5 en `src/test`); los
  listeners solo traducen eventos de Paper. Claves de texto (`"minecraft:diamond"`) en la lógica nueva.
- Nada de objetivos ni textos fijos nuevos en el código: van a YAML (desde la tarea 1.3).
- No usar `_` como identificador (es palabra reservada desde Java 9).
- No copiar código de BingoReloaded ni minecraft-bingo (GPL-3.0); sí sus ideas.

## Comandos
```bash
mvn -B verify          # compila, corre tests y deja target/AdaptiveBingo-1.0.0.jar
```
Para probar: copiar el `.jar` (o el artefacto `adaptive-bingo-jar` del CI) a `plugins/` de un servidor
Paper de la misma versión del `pom` y reiniciar.

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
