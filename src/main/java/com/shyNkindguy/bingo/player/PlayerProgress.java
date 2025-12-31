package com.shyNkindguy.bingo.player;

public class PlayerProgress {
   private int completed = 0;

   public void increment(){
       completed++;
   }

   public int getcompleted(){
        return completed;
    }
}
