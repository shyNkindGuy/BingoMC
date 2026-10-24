# AdaptiveBingo (BingoMC)

Plugin de bingo para servidores **Paper 1.21.11** de Minecraft (Java 21). Cada jugador recibe una cartilla 5×5 de
objetivos dibujada en un mapa en la mano izquierda; gana el primero en completar una línea (fila,
columna o diagonal). Si pasan 20 minutos, gana quien tenga más casillas (o empatan los que tengan más).

| Comando | Qué hace |
|---|---|
| `/bingo start` | Empieza la partida con todos los jugadores conectados (permiso `bingo.admin`, ops por defecto) |
| `/bingo stop` | Detiene la partida en curso (permiso `bingo.admin`) |
| `/bingo map` | Vuelve a dar el mapa con la cartilla |

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
