# Análisis del repositorio — 2026-10-23

Revisión completa de BingoMC (plugin `AdaptiveBingo`) para decidir cómo seguir. Diseño objetivo en
[`ARQUITECTURA.md`](ARQUITECTURA.md), orden de trabajo en [`PLAN.md`](PLAN.md).

## 1. Estado

- **Último commit:** `4705275` en `master` (12-ene-2026, merge del PR #9 `feat/icons-map`).
- **Tamaño:** 28 archivos Java, ~1.100 líneas, un solo módulo Maven, Paper 1.20.4, Java 17.
- **Qué funciona (antes del último commit):** cartilla 5×5 por jugador (mismos 25 ítems en distinto
  orden) dibujada en un mapa en la mano izquierda; detección al recoger, craftear, sacar del horno,
  llenar balde y hacer clic; victoria por línea (filas, columnas, diagonales); a los 20 min gana quien
  tenga más casillas; scoreboard lateral; `/bingo start` y `/bingo map`.
- **Dónde quedó:** el commit "refactorización de icons + pendiente" empezó un sistema nuevo de íconos
  (`icon/`: categorías, definiciones, registro, renderer) y lo dejó a medias: **`master` no compilaba**
  (17 errores: presets vacíos, faltaban `getWidth/getHeight`, `register`, `registerIngot`). Se arregló
  en la tarea 0.1 completando lo mínimo; la unificación con el sistema viejo es la tarea 0.5.

## 2. Ramas

Todas fusionadas en `master`; no hay trabajo suelto.

| Rama | Último commit | PR | Estado |
|---|---|---|---|
| `feat/core-bingo` | `a8a9cf8` | #2, #3 | fusionada |
| `feat/bingo5x5` | `786c6ec` | #4, #5 | fusionada |
| `fix/cube-issue` | `ab430e1` | #6, #7 | fusionada |
| `feat/icons-map` | `6f4c170` | #8, #9 | fusionada (deja el build roto) |
| `claude/compassionate-davinci-y5x96q` | — | — | rama de trabajo de Claude, sale de `master` |

Se trabaja siempre desde `master` actualizado. Las ramas `feat/*` y `fix/*` viejas se pueden borrar
cuando quieras (no tienen nada que no esté en `master`).

## 3. Bugs y deuda encontrados

| # | Problema | Dónde | Tarea |
|---|---|---|---|
| 1 | `master` no compila (paquete `icon/` a medias) | `icon/*` | 0.1 ✔ |
| 2 | `/bingo start` siempre manda también "Uso: /bingo start" (falta `return`) | `BingoCommand` | 0.3 ✔ |
| 3 | El timer de 20 min no se cancela: si empiezas otra partida antes, la corta | `BingoGame.start` | 0.3 ✔ |
| 4 | Llenar un balde no redibuja el mapa ni revisa victoria | `ObjectiveListener.onBucketFill` | 0.3 ✔ |
| 5 | Cualquiera puede iniciar; no hay `stop`, permisos ni reset de scoreboard | `BingoCommand`, `plugin.yml` | 0.3 ✔ |
| 6 | Clic en un ítem de un cofre (sin sacarlo) completa la casilla | `onInventoryClick` | 0.4 ✔ |
| 7 | El mapa reemplaza la mano izquierda (borra escudo/tótem) y se cae al morir | `BingoMapManager.giveMap` | 0.4 ✔ |
| 8 | Objetivos con id/nombre/ítem que no coinciden ("Diamante" → `POLISHED_DIORITE_SLAB`, "Barca de Acacia" → `WHITE_WOOL`…) y sin pool | `ObjectiveFactory` | 0.5 ✔ |
| 9 | Dos sistemas de íconos en paralelo; `rendered` nunca se usa (redibuja 128×128 cada tick) | `game/MapIconRegistry`, `icon/`, `BingoMapRenderer` | 0.5 ✔ |
| 10 | `ObjectiveType` sin usar; todo es `Material` | `game/` | 1.2 ✔ |
| 11 | `pom.xml`: `java.version 17` pero compilaba a 16, sin tests ni CI | `pom.xml` | 0.1 ✔ |

## 4. Referencias (repos con esto ya hecho)

| Proyecto | Qué tomar | Licencia |
|---|---|---|
| [BingoReloaded](https://github.com/Steaf23/BingoReloaded) (Paper, 700+ commits) | módulos `common`/`paper`; `gameloop/phase` (`PregameLobby`, `BingoGame`, `PostGamePhase`); `tasks/data` (`ItemTask`, `AdvancementTask`, `StatisticTask`) con *trackers*; cartas armadas desde listas con mín/máx; modos Regular, Complete-X, Lockout, Hot-Swap, Blitz; votación de modo; [modos](https://github.com/Steaf23/BingoReloaded/wiki/Gamemodes), [cartas](https://github.com/Steaf23/BingoReloaded/wiki/Custom-Bingo-Cards) | GPL-3.0 |
| [minecraft-bingo](https://github.com/Extremelyd1/minecraft-bingo) (Paper) | equipos con colores y dispersión; tiers S–D con `/itemdistribution`; condiciones de victoria (línea, carta llena, lockout, timer); pregeneración de mundo y world border | GPL-3.0 |
| [Bingo mod (Fabric/NeoForge)](https://www.modpackindex.com/mod/117479/bingo) | objetivos, dificultades y tags 100 % por datos (JSON en datapacks); tablero visible en pantalla | ver su repo |
| [Lockout Bingo](https://www.curseforge.com/minecraft/mc-mods/lockout-bingo) | reglas de lockout (la casilla es del primero) | ver su página |

**Ojo con la licencia:** los dos plugins de Paper son GPL-3.0. Leer sus ideas y su diseño está bien;
**copiar código** obliga a que este repo también sea GPL-3.0. Este repo todavía no tiene licencia: si
lo vas a publicar, decide una (MIT si quieres que cualquiera lo use; GPL-3.0 si quieres copiar código de
ellos).

## 5. Viabilidad

- **Como proyecto personal / aprendizaje: muy viable.** La base es chica y entendible; el plan te lleva
  por lo que más enseña en Java de servidor: eventos, máquina de estados, diseño por datos, tests,
  persistencia y CI. Unas 25 tareas, cada una un PR chico.
- **Como plugin público: viable solo con diferencial.** BingoReloaded ya cubre el bingo "clásico" muy
  completo. Lo que este plugin puede tener distinto: (1) **la cartilla en un mapa en la mano**, sin
  resource pack ni mod en el cliente; (2) **español primero**; (3) **la capa propia** de logros,
  objetos especiales, conjuntos y estados (Fase 3), que los otros no tienen.
- **Como servidor: decidir después de la Fase 2.** Un servidor necesita jugadores, hosting (desde un VPS
  chico o un host de Minecraft) y moderación. Recomendación: terminar Fases 0–2, jugar con amigos en un
  servidor local o barato, y recién ahí decidir la Fase 4 (por eso la automatización no la empieza).
- **Versión:** Paper ya va en 26.x (Java 25). Se eligió **1.21.11 + Java 21** como paso estable
  (BingoReloaded también la soporta); con el núcleo separado de Bukkit (tarea 1.1), subir a 26.x después
  toca solo el módulo `plugin`.

## 6. Decisiones del dueño (2026-10-23)

| Tema | Decisión |
|---|---|
| Alcance | Primero el motor del bingo (tipos de casilla, conjuntos, condiciones, estados de partida); después, encima, la capa de servidor (logros y estadísticas persistentes, ítems especiales, sets, estados) |
| Versión | Paper 1.21.11 + Java 21 |
| Aprobación | Claude fusiona el PR cuando el CI está verde (1 PR a la vez) |
| Ritmo | Ventanas de 5 h, todos los días. Desde 2026-10-24: trabajo continuo hasta el límite de uso (al ponerse verde el CI, fusionar y seguir); la tarea programada de cada hora retoma cuando el límite se reinicia |
