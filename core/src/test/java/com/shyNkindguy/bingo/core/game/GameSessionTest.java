package com.shyNkindguy.bingo.core.game;

import org.junit.jupiter.api.Test;

import static com.shyNkindguy.bingo.core.game.GameSession.Signal.COUNTDOWN;
import static com.shyNkindguy.bingo.core.game.GameSession.Signal.NONE;
import static com.shyNkindguy.bingo.core.game.GameSession.Signal.STARTED;
import static com.shyNkindguy.bingo.core.game.GameSession.Signal.TIME_UP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {

    @Test
    void empiezaEnElLobbySinPartida() {
        GameSession sesion = new GameSession();
        assertEquals(Phase.LOBBY, sesion.phase());
        assertFalse(sesion.inGame());
        assertEquals(NONE, sesion.tick());
        assertFalse(sesion.finish());
    }

    @Test
    void cuentaRegresivaYDespuesEnCurso() {
        GameSession sesion = new GameSession();

        assertEquals(COUNTDOWN, sesion.begin(3, 60));
        assertEquals(Phase.COUNTDOWN, sesion.phase());
        assertEquals(3, sesion.countdownLeft());
        assertTrue(sesion.inGame());

        assertEquals(COUNTDOWN, sesion.tick());
        assertEquals(2, sesion.countdownLeft());
        assertEquals(COUNTDOWN, sesion.tick());
        assertEquals(1, sesion.countdownLeft());
        assertEquals(STARTED, sesion.tick());
        assertEquals(Phase.RUNNING, sesion.phase());
        assertEquals(0, sesion.elapsedSeconds());
    }

    @Test
    void sinCuentaRegresivaArrancaEnElActo() {
        GameSession sesion = new GameSession();
        assertEquals(STARTED, sesion.begin(0, 60));
        assertEquals(Phase.RUNNING, sesion.phase());
    }

    @Test
    void alCumplirseLaDuracionAvisaQueSeAcaboElTiempo() {
        GameSession sesion = new GameSession();
        sesion.begin(0, 3);

        assertEquals(NONE, sesion.tick());
        assertEquals(NONE, sesion.tick());
        assertEquals(TIME_UP, sesion.tick());
        assertEquals(3, sesion.elapsedSeconds());
        assertTrue(sesion.finish());
        assertEquals(Phase.ENDED, sesion.phase());
    }

    @Test
    void sinLimiteNuncaSeAcabaElTiempo() {
        GameSession sesion = new GameSession();
        sesion.begin(0, 0);
        for (int i = 0; i < 10_000; i++) assertEquals(NONE, sesion.tick());
        assertEquals(10_000, sesion.elapsedSeconds());
    }

    @Test
    void noSeEmpiezaOtraEncimaDeUnaEnCurso() {
        GameSession sesion = new GameSession();
        sesion.begin(5, 60);
        assertEquals(NONE, sesion.begin(0, 60));
        assertEquals(Phase.COUNTDOWN, sesion.phase());
    }

    @Test
    void seDetieneDuranteLaCuentaRegresivaYDespuesSeVuelveAEmpezar() {
        GameSession sesion = new GameSession();
        sesion.begin(5, 60);
        assertTrue(sesion.finish());
        assertEquals(Phase.ENDED, sesion.phase());
        assertEquals(NONE, sesion.tick());

        assertEquals(COUNTDOWN, sesion.begin(2, 60));
        assertEquals(2, sesion.countdownLeft());
        sesion.tick();
        assertEquals(STARTED, sesion.tick());
        assertEquals(0, sesion.elapsedSeconds());
    }

    @Test
    void tiemposNegativosSonError() {
        assertThrows(IllegalArgumentException.class, () -> new GameSession().begin(-1, 60));
    }
}
