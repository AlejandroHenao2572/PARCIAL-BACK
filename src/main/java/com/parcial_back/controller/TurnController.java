package com.parcial_back.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.parcial_back.model.Turn;
import com.parcial_back.service.TurnService;


@RestController
@RequestMapping("/api/turn")
public class TurnController {

    private final TurnService turnService;

    public TurnController(TurnService turnService) {
        this.turnService = turnService;
    }

    @GetMapping("/ticket")
    public ResponseEntity<Turn> getTicket(){
        return(ResponseEntity.ok(turnService.getTicket()));
    }

    @GetMapping("/check/ticket")
    public ResponseEntity<Turn> checkTicket(){
        return(ResponseEntity.ok(turnService.checkTicket()));
    }
}
