# AdaptiveBingo (BingoMC)

Plugin de **bingo para Minecraft (Paper)**, proyecto personal de shyNkindguy. Cada equipo (quien no
tiene equipo juega solo) recibe una cartilla N×N de tareas dibujada en un mapa en la mano izquierda; gana
quien complete una línea (y más adelante, otros modos). A futuro: capa de servidor con logros,
estadísticas, ítems especiales, conjuntos y estados. Diseño en `docs/ARQUITECTURA.md`, diagnóstico y
viabilidad en `docs/ANALISIS.md`, orden de trabajo en `docs/PLAN.md`.

## Stack
- **Java 21, Paper API 1.21.11** (desde la tarea 0.2), Maven **multi-módulo** (desde la 1.1): `pom.xml`
  padre (versión, Java y versiones de plugins de Maven) + **`core/`** (lógica del bingo **sin Bukkit**,
  paquete `com.shyNkindguy.bingo.core`; JUnit y SnakeYAML `provided`, que en el servidor la pone Paper) + **`plugin/`** (adaptador Paper, artifactId
  `AdaptiveBingo`; `maven-shade-plugin` mete las clases de `core` en su `.jar`). La versión sale solo del
  `pom` padre (el `plugin.yml` usa `${project.version}` con resource filtering; el CI revisa que quede
  reemplazada y que el `.jar` traiga `core`).
- `core` (nada de `org.bukkit` ahí):
  - `task/`: `Task` (`id`, `displayName`, `icon` = clave del ítem a dibujar o null, `target`,
    `apply(evento, contexto, progreso)`, `base()`): `ItemTask(id, nombre, ítem, cantidad[, ícono])` (el
    máximo que tuvo a la vez), `KillTask` (mob + cantidad; ícono por defecto, su huevo), `AdvancementTask`
    (logro; sin ícono, el plugin usa el del logro), `StatisticTask` (estadística [+ `subject`
    bloque/ítem/mob] ≥ cantidad desde que empezó la partida; ícono por defecto, el `subject`), `SetTask`
    (alguno/todos: N ítems distintos de una lista a la vez) y `ConditionalTask` (otra tarea + `Condition`;
    `base()` da la de adentro: usarlo para preguntar el tipo).
  - `condition/`: `Condition.test(EventContext)` + `key()`: `InDimension`, `TimeOfDay` (noche = 13000–22999),
    `InBiome`, `BelowY`, `NoDeath`, `AllOf`, `AnyOf`.
  - `event/`: `TaskEvent` sellado: `ItemObtained(ítem, total)`, `InventoryScanned(totales)`,
    `EntityKilled(mob)`, `AdvancementDone(logro)`, `StatisticReached(estadística, subject, valor desde el
    inicio)`; y `EventContext(dimensión, bioma, hora, Y, muertes)` (`NONE`: no se cumple ninguna condición).
  - `progress/`: `TaskProgress(current, target)` inmutable que solo sube, `ProgressTracker.apply(cartilla,
    evento)` → `ProgressResult` (casillas que avanzaron y tareas completadas) y `StatisticBaseline`
    (valor al empezar por jugador).
  - `card/`: `BingoCard` N×N de `BingoCell` (tarea + progreso + `completedAt` para desempatar y `completedBy`
    para el resumen + `lockedBy` en lockout; `ProgressTracker` saltea las bloqueadas).
  - `catalog/` (desde la 1.3): `CatalogLoader` lee `cards.yml` + `tasks/*.yml` de una carpeta o los por
    defecto del `.jar` (`core/src/main/resources/adaptivebingo/defaults/`, 67 tareas de todos los tipos en
    `basicos` D, `medios` C y `dificiles` A; cartillas `normal` 5×5 y `rapida` 3×3) y los copia la primera vez;
    `CatalogParser` valida todo con mensajes "archivo, tarea N: problema"; `Catalog` (listas `TaskList` +
    `Tier`, cartillas `CardSpec` con mín/máx por lista) y `CardGenerator` (semilla, sin repetidos).
    `AchievementParser` (desde la 3.2) lee `achievements.yml` con los mismos ayudantes (y las mismas
    `condiciones`) de `CatalogParser`, `SpecialItemParser` (desde la 3.3), `items.yml`, y `SetParser` (desde la
    3.4), `sets.yml`.
  - `win/`: `WinMode` sellado (desde la 2.3: `Lines(n)`, `Blackout`, `CompleteX(n)`, `Lockout`, `TimeOnly`;
    `parse("2 lineas")`, `problemWith(tamaño)`), `WinConditionChecker` (`countLines`) y `TimeLimitWinners`
    (`winners`: más casillas y, si empatan, quien llegó antes).
  - `game/` (desde la 2.1): `Phase` (`LOBBY → COUNTDOWN → RUNNING ⇄ PAUSED → ENDED`), `GameSession` (máquina de
    estados con `begin`/`tick` por segundo/`finish`, devuelve señales `COUNTDOWN`, `STARTED`, `TIME_UP`) y
    `GameSettings` (de `config.yml`: cartilla, cuenta regresiva, duración, PvP, semilla, modo y `StartRules`:
    dispersión, borde, limpiar inventario, kit y, desde la 3.4, `estados` para todos; `EndRules`: espera final, volver, limpiar; y `ChaosRules`:
    cada cuántos minutos un ítem especial, desde la 3.3), `Timers` (escudo, esperas), `ScatterPlanner`
    (equipos en círculo alrededor del spawn) y `GameSummary` (desde la 2.6: casillas por equipo con segundo y
    quién, y el MVP).
  - `text/Messages` (desde la 2.4): textos de `messages.yml` (claves con punto, `{variables}`, `&` → `§`,
    lo que falte sale de los por defecto; una clave desconocida es error).
  - `team/` (desde la 2.2): `TeamColor` (10 colores, en español o inglés), `Team` (de color o `solo`) y
    `TeamRegistry` (unir/salir, `soloIfAlone` al empezar, `removeSolos` al terminar, `randomize`).
  - `bus/` (desde la 3.1): `DomainEvents` (bus sincrónico por tipo; si un suscriptor falla, los demás lo
    reciben igual) y `DomainEvent` sellado: `CellCompleted(equipo, jugador, tarea, segundo, EventContext)`,
    `GameEnded(segundos, aborted, modo, equipos ganadores, participantes con sus muertes)` y
    `AchievementUnlocked`.
  - `stats/` (desde la 3.1): `PlayerStats` (partidas, victorias, casillas, victoria más rápida y, desde la
    3.2, los logros desbloqueados), `StatsBook`
    (al día con los eventos; las partidas detenidas no cuentan) y `StatsRepository` →
    `YamlStatsRepository` (`stats.yml` por UUID, validado al leer, se guarda con archivo temporal + move).
  - `achievement/` (desde la 3.2): `Achievement` (id, nombre, descripción, ícono y regla), `AchievementRule`
    sellada (`GameRule`: ganar, en menos de, modo, casillas en la partida, sin morir, totales; `CellRule`:
    tarea, `Condition` del motor, en menos de) y `AchievementTracker` (mira los eventos, anota en el
    `StatsBook` y publica `AchievementUnlocked` la primera vez).
  - `item/` (desde la 3.3): `SpecialItem` (id, nombre, descripción, ítem de Minecraft, efecto), `ItemEffect`
    sellado (`Reroll`, `BiomeCompass(radio)`, `PvpShield(segundos)`), `Reroll` (casilla pendiente al azar →
    otra tarea de la misma lista que no esté en ninguna cartilla; `BingoCell.replaceTask`) y `BiomeTargets`
    (biomas que piden las casillas pendientes).
  - `status/` (desde la 3.4): `PlayerStatus` (nombre del YAML → efecto de Minecraft + nivel; `parse("prisa 2")`,
    `description()`), `ItemSet` (piezas, cuántas hacen falta, estados) y `StatusResolver` (estados de la partida
    + los de los conjuntos con piezas suficientes; si se repite un efecto, el nivel más alto).
- `plugin`: `adapter/Keys` traduce `Material`/`EntityType`/`Statistic`/`Advancement` ↔ clave;
  `config/CatalogService` tiene el catálogo (carga al arrancar, `/bingo reload`) y `config/TaskAvailability`
  dice si una tarea se puede hacer en esta versión y por qué no (las que no, no salen); el resto como abajo.
- `plugin.yml` clásico de Bukkit (no `paper-plugin.yml`). Main: `com.shyNkindguy.bingo.AdaptiveBingo`.
- Paquete `com.shyNkindguy.bingo` del plugin: `commands/` (`/bingo start|stop|pause|map|card|stats|logros|item|conjunto|reload|team`;
  `TeamCommand`), `config/`, `storage/StatsService` (carga `stats.yml`, suscribe el `StatsBook` a
  `BingoGame.events()` y guarda al terminar cada partida; con un error no pisa el archivo),
  `events/` (`ObjectiveListener`, `ScanQueue`, `CardUpdater`, `StatisticPoller`, `AdvancementReset`),
  `events/SessionListener` (congelar en la cuenta regresiva, PvP, entrar a mitad, chat de equipo, clics en
  `/bingo card`), `ui/` también con `Texts`, `GameBossBar`, `CardMenu` y `AchievementMenu` (los dos `ReadOnlyMenu`),
  `AchievementAnnouncer` y `TeamColors`, `items/` (`SpecialItems`: ItemStack con PDC `adaptivebingo:especial`
  o `adaptivebingo:pieza`; `ChaosDealer`), `game/StatusApplier` (estados por segundo), `events/SpecialItemListener` (uso con clic derecho y escudo), `game/` (`StartActions`,
  `EndActions`, `BingoGame` sobre `GameSession`, con `onStart` al terminar la cuenta y `onEnd`; `PlayerContexts`: el `EventContext`
  de cada jugador y sus muertes desde el inicio), `icon/` (+ `TaskIcons`: ícono y marca por tipo), `ui/` (mapa,
  scoreboard, paleta). Íconos: un solo sistema, `icon/IconRegistry` (por `Material`, con fallback por
  categoría) + `icon/IconRenderer` (escala entera que entra en la casilla, centrado; dibuja sobre un
  `PixelSink` para poder testearlo).

## Cómo se juega hoy
`/bingo start [cartilla] [semilla]` (permiso `bingo.admin`, ops por defecto; también desde la consola)
crea una cartilla por jugador conectado (la de `config.yml` o la `por_defecto` de `cards.yml`: `normal` 5×5) con tareas
sorteadas **una vez por partida** de `plugins/AdaptiveBingo/tasks/*.yml` (ítem + nombre en español +
cantidad, p. ej. "16 × Tronco de roble"; id `item:<ítem>`), las mismas para todos en distinto orden; anuncia
la semilla (la misma semilla repite las tareas) y da el mapa y el scoreboard. Después viene la cuenta
regresiva (`cuenta_regresiva` de `config.yml`, 10 s; título con el número, nadie se mueve, rompe, pone,
agarra ni recibe daño) y recién al "¡YA!" las tareas cuentan (`onStart`: contextos, logros, estadísticas).
Quien entra con la partida empezada juega con la cartilla de su equipo (o solo, con una nueva de las mismas
tareas; si volvió, con la suya). `pvp: false` evita que los jugadores se peguen; nadie le pega a su equipo.
**Equipos** (2.2): `/bingo team unir <color>|salir|random [n]|lista`, solo en el lobby; la cartilla, el
progreso, los avisos y el mapa son del equipo (cualquiera completa por todos; las kills se suman, las
estadísticas cuentan por jugador); el scoreboard muestra la tabla de equipos y pinta los nombres;
`#mensaje` en el chat va solo al equipo.
**UX** (2.4): bossbar con la cuenta regresiva o el tiempo que queda (roja en el último minuto); al completar
una casilla, título + sonido al equipo y anuncio a los demás con el color del equipo; `/bingo card` abre la
cartilla en un cofre de solo mirar con tipo, avance, condiciones y bloqueo en la descripción; scoreboard
sin parpadeo (texto en prefijos de `Team` sobre entradas fijas); todos los textos en `messages.yml`.
**Inicio** (2.5, `game/StartActions` con `BingoGame.onDeal`): al repartir, vaciar inventarios
(`limpiar_inventario`), repartir a los equipos en círculo a `dispersion` bloques del spawn del mundo
principal y poner el borde (`borde`; al terminar vuelve el de antes); al "¡YA!", el `kit`. Quien entra a
mitad recibe lo mismo. `/bingo pause` (`bingo.admin`) congela la partida: el tiempo no corre, nada cuenta
y nadie se mueve; otra vez `/bingo pause` sigue. `/bingo reload` relee los YAML (también `config.yml`): con
un error lo dice y sigue con lo anterior (al arrancar con error, usa los del `.jar`). El mapa usa un renderer contextual (cada jugador ve su cartilla) que solo
redibuja tras `invalidate()`. Una casilla se completa cuando el ítem está **en el inventario del jugador**: recoger, clic,
arrastrar, cerrar el inventario, craftear, horno o balde piden revisar el inventario un tick después
(`ObjectiveListener.requestScan`; `ScanQueue` junta los pedidos del mismo tick), nunca se mira el ítem
del evento; el escaneo suma cuántos tiene de cada ítem y manda `ItemObtained(ítem, total)` a la cartilla.
Matar un mob (`EntityDeathEvent` con `getKiller`) manda `EntityKilled` y un logro
(`PlayerAdvancementDoneEvent`) `AdvancementDone`; las estadísticas se leen cada segundo
(`StatisticPoller`) y cuentan desde el inicio. Al empezar, a cada jugador se le quitan los logros de su
cartilla (`AdvancementReset`), si no nunca podría volver a conseguirlos. Todo pasa por
`CardUpdater.apply`, que arma el `EventContext` del jugador (para las condiciones) y se encarga del
mensaje, el scoreboard, el mapa y la victoria; el escaneo además manda `InventoryScanned` (conjuntos) y
cambiar de mundo pide un escaneo. En el mapa, las tareas de matar, logro y estadística llevan una marca
5×5 arriba a la izquierda (espada, estrella, barras), los conjuntos dibujan sus dos primeros ítems y las
que tienen condiciones un punto morado abajo a la izquierda.
En el mapa, las tareas de varias unidades muestran la cantidad pedida y una barra de avance. La cartilla es un mapa marcado con PDC `adaptivebingo:cartilla` (`BingoMapManager.isBingoMap`):
no pisa la mano izquierda (lo que había pasa al inventario o se suelta), no se duplica, no cae al morir y
no se puede tirar. Gana según el `modo` de `config.yml` (por defecto, la primera línea; también N líneas,
carta llena, N casillas, lockout —la tarea es del primer equipo que la completa y a los demás se les
bloquea con una cruz roja; gana más de la mitad o, sin casillas libres, el que más tenga— y por tiempo); al
terminar la `duracion` de `config.yml` (20 min; 0 = sin límite), el que tenga más casillas (desempata
quien llegó antes) o empate
entre los que más tengan (`TimeLimitWinners`). `/bingo stop` la detiene. Toda partida termina por
`BingoGame.finish(ganadores)`: cancela el timer, manda el resumen al chat, guarda `lastWinners` y
`lastParticipants`, corre los `onEnd` (scoreboard principal de vuelta, mapas quitados del inventario y
`EndActions`) y borra las cartillas.
**Fin** (2.6): el resumen lista cada equipo con sus casillas (las de los ganadores una por una con el tiempo
y quién la completó) y el MVP; `game/EndActions` tira fuegos artificiales del color del equipo ganador
durante `espera_final` segundos (10) y después, si `volver_al_terminar`, devuelve a cada jugador a donde
estaba al empezar (`StartActions.origins`) y, si `limpiar_al_terminar`, vacía los inventarios de quienes
jugaron. Repartir una partida nueva cancela lo pendiente.
**Estadísticas** (3.1): `BingoGame` publica `CellCompleted` al completar una casilla y `GameEnded` en
`finish` (con `/bingo stop`, `aborted`); `StatsService` las suma en `plugins/AdaptiveBingo/stats.yml` y
`/bingo stats [jugador]` (para todos) muestra partidas, victorias y %, casillas y promedio, y la victoria
más rápida.
**Logros** (3.2): `achievements.yml` (14 por defecto) con `al: partida` (se revisa al terminar, no las
detenidas, con las estadísticas ya sumadas) o `al: casilla` (al completar una, con el `EventContext` de ese
momento: cuentan aunque después se detenga la partida). Se leen con `CatalogService` (y `/bingo reload`);
`StatsService` arma el `AchievementTracker` y guarda `stats.yml` también al desbloquear;
`AchievementAnnouncer` avisa en el chat y con título + sonido 2 s después; `/bingo logros` abre la GUI.
Las muertes de cada uno salen de `PlayerContexts` (`BingoGame.countDeathsWith`; desconectado = -1, no
cuenta para `sin_morir`).
**Ítems especiales** (3.3): `items.yml` (dado, brújula, escudo) y `config.yml` `caos` (cada N minutos de juego
un ítem al azar para cada jugador, `ChaosDealer` en `onTick`; 0 = apagado); `/bingo item <id> [jugador]`
(`bingo.admin`). Llevan la marca PDC: el escaneo no los cuenta, al terminar se quitan y solo sirven con la
partida en curso. Dado: `BingoGame.reroll` (en lockout cambia la tarea en todas las cartillas y en las de
los que entren; nunca sale una de logro). Brújula: `World.locateNearestBiome` con los biomas de las
pendientes y la apunta como lodestone, sin gastarse (espera 10 s). Escudo: `Timers` con el segundo de
partida; `SpecialItemListener.onHit` cancela el golpe de un jugador.
**Conjuntos y estados** (3.4): `config.yml` `estados` (para todos durante la partida) y `sets.yml` (4 conjuntos:
piezas marcadas; con `necesita` piezas distintas a la vez, en cualquier lugar del inventario, sus `estados`).
Las piezas salen en el caos (junto con los ítems especiales) o con `/bingo conjunto <id> [jugador]`; no cuentan
en el escaneo y se quitan al terminar. `StatusApplier` (en `onTick`, solo en curso) pone cada efecto por 20 s
(`Registry.EFFECT`) cada segundo, saca los que ya no tocan, avisa al activar o perder un conjunto y limpia al
terminar. `/bingo map` vuelve a dar el mapa. Bugs conocidos:
tabla de `docs/ANALISIS.md` §3.

## Convenciones
- Commits, mensajes del juego y documentación **en español**. Java (no migrar a Kotlin sin pedirlo).
- Lógica nueva **sin Bukkit** siempre que se pueda (clases puras con tests JUnit 5 en `src/test`); los
  listeners solo traducen eventos de Paper. Claves de texto (`"minecraft:diamond"`) en la lógica nueva.
- Nada de objetivos en el código: van a `tasks/*.yml` (desde la 1.3), y los por defecto a
  `core/src/main/resources/adaptivebingo/defaults/` (si se agrega un archivo, sumarlo a
  `CatalogLoader.DEFAULT_FILES`).
- Nada de textos para el jugador en el código (desde la 2.4): `Texts.get("grupo.clave", "variable", valor)`
  y la clave en `defaults/messages.yml` y en `TextsTest`. Los textos que vienen del núcleo (nombres de
  tareas, `WinMode.description`, `Condition.description`, `Team.label`, errores de los YAML) quedan en
  español en `core`.
- Todo YAML nuevo se valida al leer, con mensajes en español que digan archivo y entrada, y claves
  desconocidas = error (atrapa errores de tipeo).
- No usar `_` como identificador (es palabra reservada desde Java 9).
- No copiar código de BingoReloaded ni minecraft-bingo (GPL-3.0); sí sus ideas.

## Comandos
```bash
mvn -B verify            # compila los dos módulos, corre tests y deja plugin/target/AdaptiveBingo-<versión>.jar
mvn -B -pl core verify   # solo el núcleo: no necesita paper-api (corre en el contenedor de Claude)
```
Para probar: copiar el `.jar` (o el artefacto `adaptive-bingo-jar` del CI) a `plugins/` de un servidor
Paper 1.21.11 (Java 21) y reiniciar.

**El contenedor en la nube de Claude no llega a `repo.papermc.io`** (y Maven Central a veces responde
429): ahí solo `mvn -pl core verify` funciona de verdad. El módulo `plugin` se compila con `javac`
contra stubs mínimos de Bukkit en el scratchpad + el `.jar` de `core` + `snakeyaml-2.2.jar` de `~/.m2`, y sus tests con
`junit-platform-console-standalone`. La validación real del plugin es `.github/workflows/build.yml` en
cada PR. Lógica nueva: ponerla en `core` siempre que no necesite Bukkit.

En los tests del plugin no hay servidor: `Material.isBlock()`, `isItem()`, `isEdible()`, `Registry.*`,
`Bukkit.*` y los logros revientan ("No RegistryAccess implementation found"). Sí andan los enums
(`Material.matchMaterial`, `EntityType`, `Statistic` y sus `getKey()`). Lo que necesite el servidor se
inyecta (como en `TaskAvailability`).

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
