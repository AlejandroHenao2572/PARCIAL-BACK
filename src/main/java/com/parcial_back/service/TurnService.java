package com.parcial_back.service;

import org.springframework.stereotype.Service;

import com.parcial_back.model.Turn;
import com.parcial_back.persistence.TurnPersistence;

@Service
public class TurnService {

    private final TurnPersistence turnPersistence;

    public TurnService(TurnPersistence turnPersistence) {
        this.turnPersistence = turnPersistence;
    }

    public synchronized Turn getTicket() {
        Turn nuevoTurn = new Turn(generateTurnId(), "CREATED");
        turnPersistence.saveTurn(nuevoTurn.getId(), nuevoTurn);
        return nuevoTurn;
    }

    public synchronized Turn checkTicket() {
        Turn lastTurn = turnPersistence.getLastCreatedTurn();
        if (lastTurn == null) {
            return null; 
        }
        if (lastTurn.getStatus().equals("CREATED")) {
            lastTurn.setStatus("CALLED");
            turnPersistence.saveTurn(lastTurn.getId(), lastTurn);
        }
        return lastTurn;
    }

    private int generateTurnId() {
        return turnPersistence.getTurnCount() + 1;
    }

}
