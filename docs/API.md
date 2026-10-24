# API de AdaptiveBingo para otros plugins

Desde la tarea 3.5. Dos formas de leer lo que pasa en el bingo desde afuera: la **API de Java** (para otro
plugin) y los **placeholders** de PlaceholderAPI (para scoreboards, hologramas, TAB, etc.).

## API de Java

En el `plugin.yml` del otro plugin: `depend: [AdaptiveBingo]` (o `softdepend`). Para compilar, el `.jar` de
AdaptiveBingo como dependencia `provided` (trae adentro las clases de `core`).

```java
import com.shyNkindguy.bingo.api.AdaptiveBingoApi;
import com.shyNkindguy.bingo.core.bus.AchievementUnlocked;
import com.shyNkindguy.bingo.core.bus.CellCompleted;
import com.shyNkindguy.bingo.core.bus.DomainEvents;
import com.shyNkindguy.bingo.core.bus.GameEnded;
import com.shyNkindguy.bingo.core.stats.Ranking;

public final class MiPlugin extends JavaPlugin {
    private DomainEvents.Subscription casillas;

    @Override
    public void onEnable() {
        AdaptiveBingoApi bingo = Bukkit.getServicesManager().load(AdaptiveBingoApi.class);
        if (bingo == null) return; // AdaptiveBingo no está

        casillas = bingo.subscribe(CellCompleted.class, event ->
                getLogger().info(event.playerName() + " completó " + event.taskName() + " en el segundo " + event.second()));
        bingo.subscribe(GameEnded.class, event -> {
            if (!event.aborted()) getLogger().info("Ganaron: " + event.winners());
        });
        bingo.subscribe(AchievementUnlocked.class, event -> getLogger().info(event.playerName() + " → " + event.name()));

        bingo.top(Ranking.Metric.WINS, 3).forEach(entry ->
                getLogger().info(entry.position() + ". " + entry.stats().name() + ": " + entry.stats().wins()));
    }

    @Override
    public void onDisable() {
        if (casillas != null) casillas.cancel();
    }
}
```

| Método | Qué da |
|---|---|
| `inGame()` | hay una partida repartida (cuenta regresiva, en curso o en pausa) |
| `isRunning()` | la partida está en curso: las tareas cuentan |
| `progress(uuid)` | casillas completas de la cartilla del equipo del jugador (0 si no juega) |
| `stats(uuid)` | `PlayerStats`: partidas, victorias, casillas, victoria más rápida (segundos, -1 si no ganó) y logros |
| `top(métrica, n)` | los `n` mejores por `WINS`, `GAMES`, `CELLS` o `FASTEST` |
| `subscribe(tipo, listener)` | los eventos de la partida; devuelve una `Subscription` para `cancel()` |

Eventos (todos en el hilo principal):
- `CellCompleted(team, playerId, playerName, taskId, taskName, second, context)`: alguien completó una casilla.
- `GameEnded(seconds, aborted, mode, winners, players)`: terminó una partida (`aborted` = `/bingo stop`;
  `winners` son ids de equipo; `players` trae id, nombre, equipo y muertes de cada uno).
- `AchievementUnlocked(playerId, playerName, achievementId, name, description)`: un logro de `achievements.yml`.

Los ids de jugador son el UUID en texto.

## Placeholders (PlaceholderAPI)

Si PlaceholderAPI está instalado, AdaptiveBingo registra `%adaptivebingo_<clave>%` solo (no hace falta
`/papi ecloud`).

| Clave | Valor |
|---|---|
| `partidas`, `victorias`, `casillas`, `logros` | del jugador, desde siempre |
| `porcentaje` | % de victorias (sin el signo) |
| `mejor_tiempo` | su victoria más rápida (`12:34`), o `-` |
| `en_partida` | `sí` / `no` |
| `equipo` | "Equipo Rojo", su nombre si juega solo, o `-` |
| `casillas_partida` | casillas de su equipo en la partida en curso |
| `top_<métrica>_<n>` | nombre del puesto `n` (métrica: `victorias`, `partidas`, `casillas`, `tiempo`) |
| `top_<métrica>_<n>_valor` | su valor (`tiempo` como `12:34`) |

Los textos `sí`, `no` y `-` se cambian en `messages.yml` (`placeholders.*`).
