package com.shyNkindguy.bingo.core.achievement;

// Cuándo se revisa un logro y qué pide: al completar una casilla (CellRule) o al terminar una partida
// (GameRule).
public sealed interface AchievementRule permits CellRule, GameRule {
}
