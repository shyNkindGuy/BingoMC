package com.shyNkindguy.bingo.player;

public class PlayerProgress {
    private int completedObjectives;
    private long lastCompletionTime;

    public void increment(){
        completedObjectives++;
        lastCompletionTime = System.currentTimeMillis();
    }
}
