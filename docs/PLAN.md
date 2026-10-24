# Plan de trabajo — AdaptiveBingo (BingoMC)

Cola de tareas en orden. Claude toma **la primera tarea sin marcar**, la hace en la rama
`claude/compassionate-davinci-y5x96q`, abre un PR contra `master` y lo fusiona solo si el CI está
verde (decisión del dueño, 2026-10-23). Reglas completas en [`CLAUDE.md`](../CLAUDE.md); diseño en
[`ARQUITECTURA.md`](ARQUITECTURA.md).

Leyenda: `[ ]` pendiente · `[x]` hecha (se marca dentro del PR que la resuelve)

## Fase 0 — Base sana (antes de agregar nada)
- [x] **0.1 Build verde + CI + documentación.** Completar el paquete `icon/` que dejaba `master` sin
  compilar, `pom.xml` con versión de compilador y JUnit 5, test de `WinConditionChecker`, workflow
  `build.yml` (compila, corre tests y sube el `.jar`), `CLAUDE.md`, `docs/` y la skill
  `.claude/skills/bingomc-contexto`.
- [x] **0.2 Migrar a Paper 1.21.11 + Java 21.** `paper-api 1.21.11-R0.1-SNAPSHOT`, `java.version 21`,
  `api-version: '1.21'`, versión del `plugin.yml` tomada del `pom` (resource filtering), CI con Java 21,
  README con los nuevos requisitos. Arreglar lo que la API nueva rompa o marque como eliminado.
- [x] **0.3 Bugs de partida.** Falta `return` en `/bingo start` (siempre muestra "Uso"); guardar el
  `BukkitTask` del límite de 20 min y cancelarlo en `end()` (hoy corta la partida siguiente) usando
  `MAX_TIME`; `onBucketFill` debe pasar por `handleProgress` (redibujar mapa y revisar victoria);
  `/bingo stop` y permiso `bingo.admin` para start/stop; quitar `/bingotest`; empates en
  `endByPoints`; al terminar, limpiar scoreboard y quitar los mapas.
- [x] **0.4 Mapa sin pérdidas y sin trampas.** No pisar la mano izquierda (lo que había pasa al
  inventario o se suelta); el mapa no se cae al morir y no se puede tirar; detectar por el inventario
  real del jugador un tick después del evento, no por el ítem clicado (hoy basta con hacer clic en un
  cofre).
- [x] **0.5 Objetivos corregidos e íconos unificados.** Los 25 objetivos con id, nombre y `Material`
  que coincidan (hoy "Diamante" pide `POLISHED_DIORITE_SLAB`); pool de más de 25 y elegir 25 al azar;
  un solo sistema de íconos (`icon/IconRegistry`) usado por `BingoMapRenderer`, borrar
  `game/IconPresets` y `game/MapIconRegistry`; renderer contextual que solo redibuja si cambió;
  implementar `drawScaled`. Test: 25 objetivos distintos.

## Fase 1 — Motor de tareas (bingo por datos)
- [x] **1.1 Módulo `core` puro.** Maven multi-módulo: `core` (sin Bukkit) y `plugin` (Paper). Al core:
  cartilla N×N, casilla, victoria y generador, con claves `"minecraft:diamond"` en vez de `Material`.
  `mvn -pl core verify` corre sin `paper-api` (también en el contenedor de Claude).
- [ ] **1.2 Modelo de tareas.** Interfaz `Task`, `TaskEvent` (sealed), `TaskProgress` (n/m),
  `ItemTask` con cantidad ("16 troncos"), `ProgressTracker`; los listeners traducen eventos de Paper a
  `TaskEvent`. Tests de progreso.
- [ ] **1.3 Tareas en YAML.** `tasks/*.yml` (listas con tier S–D) y `cards.yml` (mín/máx por lista),
  copiados por defecto en el primer arranque; `/bingo reload`; generador con semilla y sin repetidos.
  Tests del parser y del generador.
- [ ] **1.4 Tipos nuevos.** `kill` (mob con cantidad), `advancement` (logro de Minecraft),
  `statistic` (estadística ≥ N, revisión periódica). Ícono por tipo en el mapa.
- [ ] **1.5 Conjuntos y condiciones.** `any_of` ("3 de estas 5 lanas"), `all_of`; condiciones
  `dimension`, `time` (día/noche), `biome`, `below_y`, `no_death` combinables en YAML. Tests de cada una.

## Fase 2 — Partida completa
- [ ] **2.1 Sesión con fases.** `LOBBY → COUNTDOWN → RUNNING → ENDED`, `GameSettings` (tamaño 3/4/5,
  duración, modo, PvP, semilla) desde `config.yml`; quien entra a mitad recibe su cartilla. Tests de
  transiciones.
- [ ] **2.2 Equipos.** `/bingo team crear|unir|salir|random`, colores, cartilla por equipo (solo =
  equipo de 1), scoreboard con colores, chat de equipo.
- [ ] **2.3 Modos de victoria.** Línea (N líneas), Carta llena, Lockout, Complete-X, Por tiempo con
  desempate. Tests de cada modo.
- [ ] **2.4 UX en partida.** Bossbar con tiempo, título + sonido al completar, anuncio con color de
  equipo, GUI `/bingo card` con lore de progreso, scoreboard sin parpadeo, textos en `messages.yml`.
- [ ] **2.5 Inicio de partida.** Teletransporte disperso por equipo, world border, cuenta regresiva,
  kit inicial opcional, `/bingo pause`.
- [ ] **2.6 Fin de partida.** Resumen (tiempo por casilla, MVP), fuegos artificiales, vuelta al lobby y
  limpieza de inventarios.

## Fase 3 — Capa de servidor (encima del bingo)
- [ ] **3.1 Eventos de dominio + estadísticas.** `DomainEvents` (`CellCompleted`, `GameEnded`) y
  perfil persistente por jugador (`StatsRepository`, YAML): partidas, victorias, casillas, mejor
  tiempo. `/bingo stats`.
- [ ] **3.2 Logros propios.** `achievements.yml` (disparador + condiciones del motor 1.5), aviso al
  desbloquear, GUI `/bingo logros`.
- [ ] **3.3 Ítems especiales.** Ítems con `PersistentDataContainer` para un modo "Caos" (reroll de
  casilla, brújula al bioma, escudo anti-PvP), configurables en YAML.
- [ ] **3.4 Conjuntos con bonus y estados.** Llevar N piezas marcadas da un efecto; `PlayerStatus`
  (velocidad, visión nocturna, sin hambre) como modificador de partida o recompensa.
- [ ] **3.5 Ranking y API.** `/bingo top`, expansión de PlaceholderAPI opcional (`softdepend`), API
  pública para otros plugins.

## Fase 4 — Servidor (decide el dueño: la automatización no la empieza)
- [ ] **4.1 Varias partidas a la vez** (arenas, mundo por partida, pregeneración de chunks).
- [ ] **4.2 Despliegue** (Docker `itzg/minecraft-server`, backups, hosting).
- [ ] **4.3 Red con lobby** (Velocity), solo si hay jugadores.
