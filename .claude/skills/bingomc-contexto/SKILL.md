---
name: bingomc-contexto
description: Contexto del proyecto BingoMC / AdaptiveBingo, el plugin de bingo para Minecraft (Paper, Java, Maven) de shyNkindguy - qué está hecho, dónde quedó, decisiones tomadas, arquitectura objetivo y plan. Úsala siempre que se hable de BingoMC, AdaptiveBingo, el bingo de Minecraft, la cartilla en el mapa, objetivos/tareas/casillas, logros, conjuntos de objetos, condiciones, modos (lockout, carta llena) o de convertirlo en servidor, aunque no se nombre el repo.
---

# BingoMC — contexto

Repo `shyNkindGuy/BingoMC`, plugin `AdaptiveBingo` (`com.shyNkindguy.bingo.AdaptiveBingo`). Proyecto
personal, con idea de llegar a servidor. Todo en español (commits, mensajes, docs).

## Qué está hecho
- `/bingo start [cartilla] [semilla]`: cartilla por jugador (`normal` 5×5 o `rapida` 3×3 de
  `cards.yml`) con tareas sorteadas por partida de `tasks/*.yml` (67 por defecto: ítems, mobs, logros,
  estadísticas y conjuntos, algunas con condiciones; las mismas para todos, en distinto orden), `/bingo reload` para releer los YAML, mapa en la mano izquierda
  (`BingoMapManager` + `BingoMapRenderer` contextual, íconos 8×8 de `icon/IconRegistry` al doble), scoreboard
  lateral (`BingoScoreBoard`). `/bingo map` vuelve a dar el mapa; `/bingo stop` la detiene (start/stop
  piden `bingo.admin`).
- Casilla completa cuando el `Material` está en el inventario real (escaneo un tick después de
  recoger/clic/arrastrar/cerrar/craftear/horno/balde). Gana la primera línea
  (`WinConditionChecker`); a los 20 min, el que tenga más casillas o empate (`TimeLimitWinners`).
- Historia: estructura base → comando → cartilla + tablero → 5×5 con mapa y timer → fix de baldes →
  íconos en el mapa → refactor de íconos a medias (`icon/`). Todas las ramas viejas están fusionadas.

## Dónde quedó
- 2026-10-23 · tarea **0.1**: `master` no compilaba por el paquete `icon/` a medias; se completó lo
  mínimo, se agregó JUnit 5 + test de victoria, CI (`.github/workflows/build.yml`), `CLAUDE.md`, `docs/`
  y esta skill (PR #10).
- 2026-10-23 · tarea **0.2**: Paper 1.21.11 + Java 21, plugin v1.1.0, `plugin.yml` con
  `${project.version}` y `api-version: '1.21'` (PR #11).
- 2026-10-23 · tarea **0.3**: `/bingo stop` + permiso `bingo.admin`, timer cancelable, empates por
  tiempo, balde por `handleProgress`, `BingoGame.onEnd` limpia scoreboard y mapas, fuera `/bingotest`.
  (PR #12).
- 2026-10-23 · tarea **0.4**: detección por inventario real (`ScanQueue`), cartilla marcada con PDC
  que no pisa la mano izquierda, no se duplica, no cae al morir y no se tira (PR #13).
- 2026-10-23 · tarea **0.5**: pool de 43 objetivos con nombres en español (`ObjectivePool`, id derivado
  del ítem), sorteo de 25 por partida, un solo sistema de íconos (`icon/`; se borraron
  `game/IconPresets` y `game/MapIconRegistry`), `drawScaled` y renderer contextual que solo redibuja al
  cambiar (PR #14). **Fase 0 completa.**
- 2026-10-24 · tarea **1.1**: Maven multi-módulo. `core/` (sin Bukkit: `card/` con `BingoCard` N×N,
  `BingoObjective` por clave `"minecraft:…"`, `ObjectivePool.draw`; `win/`) y `plugin/` (Paper +
  `adapter/Keys`; shade mete `core` en el `.jar`, que sale en `plugin/target/`). `mvn -pl core verify`
  corre en el contenedor (PR #15).
- 2026-10-24 · tarea **1.2**: modelo de tareas en `core`: `Task`/`ItemTask` (con cantidad), `TaskEvent`
  sellado (`ItemObtained`), `TaskProgress` (solo sube) y `ProgressTracker`; `BingoCell` guarda tarea +
  progreso (adiós `BingoObjective` y `ObjectiveType`). El escaneo suma totales por ítem; el mapa muestra
  cantidad y barra (PR #16).
- 2026-10-24 · tarea **1.3**: tareas en YAML. `core/catalog/` (`CatalogLoader` con SnakeYAML `provided`,
  `CatalogParser` que valida con mensajes "archivo, tarea N: …", `Catalog`, `TaskList` + `Tier`,
  `CardSpec` con mín/máx, `CardGenerator` con semilla); por defecto `tasks/{basicos,medios,dificiles}.yml`
  y `cards.yml` (`normal` 5×5, `rapida` 3×3) que se copian a `plugins/AdaptiveBingo/` la primera vez;
  plugin `config/CatalogService`, `/bingo reload`, `/bingo start [cartilla] [semilla]` y autocompletado.
  Adiós `ObjectivePool` y `ObjectiveFactory` (PR #17).
- 2026-10-24 · tarea **1.4**: tipos `matar`/`kill` (`KillTask`, `EntityKilled`), `logro`/`advancement`
  (`AdvancementTask`, `AdvancementDone`; al empezar `AdvancementReset` quita los logros de la cartilla) y
  `estadistica`/`statistic` (`StatisticTask` + `de`, `StatisticReached`, `StatisticPoller` cada segundo con
  `StatisticBaseline`); `icono` opcional en todas; `TaskAvailability` filtra lo que no existe con motivo;
  `CardUpdater` centraliza mensaje/scoreboard/mapa/victoria; marcas 5×5 por tipo en el mapa (`TaskIcons`).
  59 tareas por defecto (PR #18).
- 2026-10-24 · tarea **1.5**: conjuntos `alguno`/`todos` (`SetTask` con `InventoryScanned`) y condiciones
  (`core/condition`: dimensión, hora, bioma, debajo de Y, sin morir, `alguna`; `ConditionalTask`,
  `EventContext`, `Task.base()`), contexto armado por `PlayerContexts` en `CardUpdater`; mapa: dos ítems
  para conjuntos y punto morado si hay condiciones. 67 tareas por defecto. **Fase 1 completa.**
  (PR #19).
- 2026-10-24 · tarea **2.1**: `core/game` (`Phase`, `GameSession` con señales por segundo, `GameSettings`)
  y `config.yml` (cartilla, `cuenta_regresiva`, `duracion`, `pvp`, `semilla`; se recarga con `/bingo
  reload`). `BingoGame` sobre `GameSession`: cuenta regresiva con títulos y jugadores congelados
  (`SessionListener`), `onStart` al "¡YA!", duración configurable, PvP apagable y cartilla para quien entra
  a mitad. Adiós `GameState` (PR #20).
- 2026-10-24 · tarea **2.2**: equipos. `core/team` (`TeamColor`, `Team` de color o solo, `TeamRegistry`);
  `BingoGame` con cartilla por equipo (`teamCards`, `teammates`, `teamInGame`; solos al empezar, se deshacen
  al terminar); `/bingo team unir|salir|random|lista` (`TeamCommand`, solo fuera de partida); avisos y
  mapas para todo el equipo; scoreboard con tabla de equipos y nombres coloreados (`TeamColors`); chat
  `#mensaje`; sin fuego amigo (PR #21).
- 2026-10-24 · tarea **2.3**: modos de victoria (`core/win/WinMode`: N líneas, carta llena, N casillas,
  lockout, por tiempo) en `config.yml` `modo` (texto en español: "2 lineas", "carta llena"…); desempate por
  tiempo (`BingoCell.completedAt`, `TimeLimitWinners.winners`); lockout bloquea la tarea en las cartillas
  de los demás (cruz roja en el mapa, aviso, fin si no quedan libres) (PR #22).
- 2026-10-24 · tarea **2.4**: UX. `messages.yml` (`core/text/Messages` + `ui/Texts`; ya no hay textos en el
  código del plugin), bossbar (`GameBossBar`, `BingoGame.onTick`), título + sonido al completar y anuncio
  con color de equipo, `/bingo card` (`CardMenu`, lore con tipo/avance/condiciones/bloqueo;
  `Condition.description`), scoreboard sin parpadeo (prefijos de `Team`) (PR #23).
- 2026-10-24 · tarea **2.5**: inicio de partida. `StartRules` + `ScatterPlanner` en `core/game`; `config.yml`
  `dispersion`, `borde`, `limpiar_inventario`, `kit`; `game/StartActions` (al repartir: inventarios,
  teletransporte por equipo a un lugar seguro, borde; al "¡YA!": kit; restaura el borde; lo mismo para
  quien entra tarde). `/bingo pause` (`Phase.PAUSED`: tiempo quieto, nada cuenta, jugadores congelados,
  bossbar "En pausa") (PR #24).
- 2026-10-24 · tarea **2.6**: fin de partida. `core/game/GameSummary` (casillas por equipo con segundo y
  quién —`BingoCell.completedBy`—, MVP) y `EndRules` (`config.yml` `espera_final`, `volver_al_terminar`,
  `limpiar_al_terminar`); `BingoGame.finish(ganadores)` manda el resumen; `game/EndActions`: fuegos
  artificiales del color del ganador, vuelta a donde empezó cada uno (`StartActions.origins`) y limpieza.
  **Fase 2 completa** (PR #25).
- 2026-10-24 · tarea **3.1**: `core/bus` (`DomainEvents`, `CellCompleted`, `GameEnded`) publicados por
  `BingoGame`; `core/stats` (`PlayerStats`, `StatsBook`, `YamlStatsRepository` → `stats.yml` validado);
  plugin `storage/StatsService` (no pisa un `stats.yml` roto) y `/bingo stats [jugador]`. Las partidas
  detenidas no cuentan (PR #26).
- 2026-10-24 · tarea **3.2**: logros propios. `achievements.yml` (14 por defecto; `al: partida` o `al:
  casilla` con las condiciones del motor) leído por `AchievementParser`; `core/achievement` (`GameRule`,
  `CellRule`, `AchievementTracker` → `AchievementUnlocked`); los desbloqueados van en `stats.yml`
  (`logros:`); aviso en el chat + título (`AchievementAnnouncer`) y GUI `/bingo logros` (`AchievementMenu`,
  `ReadOnlyMenu`). `GameEnded` lleva modo y muertes; `CellCompleted`, el `EventContext` (PR #27).
- 2026-10-24 · tarea **3.3**: ítems especiales. `items.yml` (dado `cambiar_casilla`, brújula `brujula_bioma`,
  escudo `escudo_pvp`) con `SpecialItemParser`; `core/item` (`Reroll`, `BiomeTargets`), `Timers` y
  `ChaosRules` (`config.yml` `caos`); plugin `items/SpecialItems` (PDC), `ChaosDealer`,
  `events/SpecialItemListener` y `/bingo item`. No cuentan en el escaneo y se quitan al terminar.
  (PR #28).
- 2026-10-24 · tarea **3.4**: conjuntos y estados. `core/status` (`PlayerStatus`, `ItemSet`, `StatusResolver`),
  `sets.yml` (`SetParser`) y `config.yml` `estados` (en `StartRules`); plugin: piezas en `SpecialItems` (PDC
  `pieza`), caos con piezas, `game/StatusApplier` (efectos de 20 s refrescados cada segundo) y `/bingo conjunto`.
  (PR #29).
- 2026-10-24 · tarea **3.5**: ranking y API. `core/stats/Ranking` + `/bingo top`, `core/stats/Placeholders` +
  `hooks/BingoExpansion` (PlaceholderAPI `softdepend`, dependencia `provided` de repo.extendedclip.com),
  `api/AdaptiveBingoApi` en el `ServicesManager` (`docs/API.md`), `DomainEvents.Subscription`. **Fase 3
  completa.** Lo que sigue es la **Fase 4** (servidor): la decide el dueño, la automatización no la empieza.

## Decisiones (no volver a preguntar)
| Tema | Decisión |
|---|---|
| Alcance | 1º motor del bingo (tipos de casilla: ítem con cantidad, mob, logro de MC, estadística, conjuntos any_of/all_of, condiciones dimensión/hora/bioma/Y/sin morir, fases de partida, equipos, modos). 2º capa de servidor encima (estadísticas y logros propios persistentes, ítems especiales PDC, sets con bonus, estados del jugador) |
| Versión | Paper **1.21.11 + Java 21** (26.x/Java 25 más adelante, solo toca el módulo `plugin`) |
| Aprobación | Claude fusiona su PR si el CI está verde; 1 PR a la vez; rama `claude/compassionate-davinci-y5x96q` reiniciada desde `master` tras cada merge |
| Ritmo | Continuo hasta el límite de uso: al ponerse verde el CI se fusiona y se sigue con la próxima tarea en el momento; la tarea programada de cada hora solo retoma después de que el límite se reinicia (2026-10-24) |
| Servidor (Fase 4) | Lo decide el dueño después de jugar las Fases 0–2 con amigos |

## Arquitectura objetivo (resumen de `docs/ARQUITECTURA.md`)
- **`core` sin Bukkit** (tests en cualquier JVM) + **`plugin`** Paper que traduce eventos → `TaskEvent`.
- `Task` (`apply(evento, contexto, progreso)`) con `ItemTask`, `KillTask`, `AdvancementTask`,
  `StatisticTask`, `SetTask`, `ConditionalTask` + `Condition`; `Card` N×N con `CellState` por equipo;
  `WinCondition` (línea, carta llena, lockout, complete-X, tiempo); `GameSession` con fases
  `LOBBY → COUNTDOWN → RUNNING → ENDED`; `DomainEvents` para que UI, estadísticas y logros se suscriban.
- Todo por YAML: `tasks/*.yml` (listas con tier S–D), `cards.yml` (mín/máx por lista),
  `achievements.yml`, `messages.yml`. Claves de texto `"minecraft:diamond"` en el núcleo.
- Diferencial frente a BingoReloaded / minecraft-bingo (GPL-3.0, tomar ideas, no código): cartilla en
  mapa sin resource pack, español primero, capa propia de logros/objetos/sets/estados.

## Trampas conocidas
- El contenedor de Claude no llega a `repo.papermc.io`: ahí solo corre `mvn -pl core verify`; el
  módulo `plugin` se valida con `javac` + JUnit console + stubs de Bukkit en el scratchpad y la verdad
  es el CI. Por eso la lógica nueva va en `core`.
- Maven Central puede responder 429: reintentar con espera.
- En los tests del plugin (CI) no hay servidor: `Material.isBlock()/isItem()/isEdible()`, `Registry`,
  `Bukkit.*` y logros fallan; los enums y sus `getKey()` sí andan. Inyectar lo que pida servidor.
- `_` no es identificador válido (Java 9+). Los presets de íconos usan `o` como transparente.
- La cartilla se reconoce por la marca PDC, no por el id del mapa: todo ítem de cartilla nuevo debe
  pasar por `BingoMapManager.giveMap`. Todavía se puede guardar en un cofre o un marco (no es trampa:
  solo se pierde la cartilla hasta `/bingo map`).
