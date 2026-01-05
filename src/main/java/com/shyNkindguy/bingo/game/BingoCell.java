package com.shyNkindguy.bingo.game;

public class BingoCell {
    private final BingoObjective objective;
    private boolean completed;

    public BingoCell(BingoObjective objective){
        this.objective = objective;
        this.completed = false;
    }

    public BingoObjective getObjective(){
        return objective;
    }

    public boolean isCompleted(){
        return completed;
    }

    public void setCompleted(boolean completed){
        this.completed = completed;
    }
}
