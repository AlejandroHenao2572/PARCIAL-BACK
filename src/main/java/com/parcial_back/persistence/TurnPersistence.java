package com.parcial_back.persistence;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.parcial_back.model.Turn;

@Repository
public class TurnPersistence {
    private final Map<Integer, Turn> Turns = new ConcurrentHashMap<>();

    public void saveTurn(Integer id, Turn turn){
        Turns.put(id, turn);
    }

    public Turn getLastCreatedTurn(){
        return Turns.values().stream()
                .filter(turn -> turn.getStatus().equals("CREATED"))
                .findFirst()
                .orElse(null);
    }

    public Integer getTurnCount(){
        return Turns.size();
    }
}
