package com.shyNkindguy.bingo.core.game;

// Fases de una partida: LOBBY (esperando) → COUNTDOWN (cuenta regresiva, nadie se mueve) → RUNNING (en
// curso, con pausas: PAUSED, el tiempo no corre y nadie se mueve) → ENDED (terminada; la próxima vuelve a
// empezar desde la cuenta regresiva).
public enum Phase {
    LOBBY, COUNTDOWN, RUNNING, PAUSED, ENDED
}
