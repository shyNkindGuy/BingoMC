# AdaptiveBingo (BingoMC)

Plugin de bingo para servidores **Paper** de Minecraft. Cada jugador recibe una cartilla 5×5 de
objetivos dibujada en un mapa en la mano izquierda; gana el primero en completar una línea (fila,
columna o diagonal). Si pasan 20 minutos, gana quien tenga más casillas.

| Comando | Qué hace |
|---|---|
| `/bingo start` | Empieza la partida con todos los jugadores conectados |
| `/bingo map` | Vuelve a dar el mapa con la cartilla |

**Documentación:** [arquitectura](docs/ARQUITECTURA.md) · [plan de trabajo](docs/PLAN.md) ·
[análisis y viabilidad](docs/ANALISIS.md)

## Compilar
```bash
mvn -B verify    # deja target/AdaptiveBingo-1.0.0.jar
```
Cada PR compila y corre los tests en GitHub Actions (`.github/workflows/build.yml`); el `.jar` queda
como artefacto del run.

---

## Tuto de crear BINGO PAPER MC DESDE 0, explicado por un bobo

JAVA 17 (NO ES NECESARIO LA ULTIMA VERSION, SE ELIGIRÁ EN EL IDE AL DESAROLLAR):
https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html

LUEGO, COMPROBAR en CMD(terminal):
java -version
> java version "17.0.12"

PAPERMC SERVER 1.20.4 (MEJOR COMPATIBILIDAD):
https://fill-ui.papermc.io/projects/paper/version/1.20.4

INTELLIJ IDEA (IDE JAVA RECOMENDADO):
https://www.jetbrains.com/es-es/idea/download/?section=windows

Luego, configuramos todo el IDE de preferencia INTELLIJ para utilizar la versión 17 de JAVA y elegimos MAVEN como libreria

Inicializaremos el JAR de el servidor Paper que instalamos antes, y cada que hagamos cambios en el plugin lo reemplazaremos desde la carpeta PLUGINS del Paper
