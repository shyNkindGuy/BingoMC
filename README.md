# AdaptiveBingo (BingoMC)

Plugin de bingo para servidores **Paper 1.21.11** de Minecraft (Java 21). Cada jugador o equipo recibe una cartilla
(5×5 por defecto) de objetivos dibujada en un mapa en la mano izquierda; gana el primero en completar una
línea (fila, columna o diagonal). Antes de empezar hay una cuenta regresiva; si se acaba el tiempo (20
minutos por defecto), gana quien tenga más casillas (o empatan los que tengan más).

| Comando | Qué hace |
|---|---|
| `/bingo start [cartilla] [semilla]` | Empieza la partida con todos los jugadores conectados (permiso `bingo.admin`, ops por defecto). Sin cartilla usa la por defecto de `cards.yml`; la semilla se anuncia y con la misma salen las mismas tareas |
| `/bingo stop` | Detiene la partida en curso (permiso `bingo.admin`) |
| `/bingo reload` | Vuelve a leer `cards.yml` y `tasks/*.yml`; si tienen un error, dice dónde y sigue con los de antes (permiso `bingo.admin`) |
| `/bingo map` | Vuelve a dar el mapa con la cartilla |
| `/bingo team unir <color>` · `salir` · `lista` | Armar equipos antes de la partida (rojo, azul, verde, amarillo, celeste, morado, naranja, rosa, blanco, gris). Quien no tiene equipo juega solo |
| `/bingo team random [n]` | Reparte a todos al azar en `n` equipos (2 por defecto; permiso `bingo.admin`) |

En equipo, la cartilla es compartida: lo que completa uno cuenta para todos. En el chat, `#mensaje` lo lee
solo tu equipo.

## Tareas y cartillas
La primera vez que arranca, el plugin crea en `plugins/AdaptiveBingo/`:
- `tasks/basicos.yml`, `tasks/medios.yml`, `tasks/dificiles.yml`: listas de tareas, cada una con su
  dificultad (`tier` S–D). Cualquier otro `.yml` en `tasks/` es otra lista. Tipos de tarea: tener un ítem
  (`item`, con cantidad), matar mobs (`matar`), conseguir un logro de Minecraft (`logro`; al empezar se le
  quitan a cada jugador los logros de su cartilla para que cuenten), llegar a una estadística desde que
  empezó la partida (`estadistica`: saltos, bloques picados…) y conjuntos (`alguno`: "3 lanas de colores
  distintos"; `todos`: "las 3 herramientas de hierro"). Cualquier tarea puede llevar `condiciones`:
  dimensión, de día o de noche, bioma, debajo de cierta Y o sin haber muerto (en el mapa, un punto morado).
- `cards.yml`: las cartillas (`normal` 5×5 y `rapida` 3×3) con cuántas tareas salen de cada lista.
- `config.yml`: cómo se juega: cartilla por defecto, segundos de cuenta regresiva (10; durante la cuenta
  nadie se mueve), minutos de partida (20; 0 = sin límite), PvP, una semilla fija opcional y el modo:
  `linea` (o `2 lineas`…), `carta llena`, `10 casillas`, `lockout` (la casilla es del primer equipo que la
  completa) o `tiempo` (gana quien tenga más al final; si empatan, quien llegó primero).

Quien entra al servidor con la partida empezada recibe su cartilla con las mismas tareas.

El formato está explicado en los comentarios de los propios archivos. Después de editar: `/bingo reload`.

**Documentación:** [arquitectura](docs/ARQUITECTURA.md) · [plan de trabajo](docs/PLAN.md) ·
[análisis y viabilidad](docs/ANALISIS.md)

## Compilar
```bash
mvn -B verify            # deja plugin/target/AdaptiveBingo-<versión>.jar (versión del pom.xml padre)
mvn -B -pl core verify   # solo el núcleo del bingo (no necesita el repositorio de Paper)
```
El proyecto tiene dos módulos: `core/` (la lógica del bingo, sin Bukkit) y `plugin/` (lo de Paper:
comandos, eventos, mapa); el `.jar` del plugin ya lleva `core` adentro.
Cada PR compila y corre los tests en GitHub Actions (`.github/workflows/build.yml`); el `.jar` queda
como artefacto del run.

---

## Tuto de crear BINGO PAPER MC DESDE 0, explicado por un bobo

JAVA 21 (NO ES NECESARIO LA ULTIMA VERSION, SE ELIGIRÁ EN EL IDE AL DESAROLLAR):
https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html

LUEGO, COMPROBAR en CMD(terminal):
java -version
> java version "21.0.x"

PAPERMC SERVER 1.21.11 (LA QUE USA EL PLUGIN; PAPER 1.21 PIDE JAVA 21):
https://fill-ui.papermc.io/projects/paper/version/1.21.11

INTELLIJ IDEA (IDE JAVA RECOMENDADO):
https://www.jetbrains.com/es-es/idea/download/?section=windows

Luego, configuramos todo el IDE de preferencia INTELLIJ para utilizar la versión 21 de JAVA y elegimos MAVEN como libreria

Inicializaremos el JAR de el servidor Paper que instalamos antes, y cada que hagamos cambios en el plugin lo reemplazaremos desde la carpeta PLUGINS del Paper
