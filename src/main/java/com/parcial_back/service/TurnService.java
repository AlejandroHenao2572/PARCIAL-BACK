package com.parcial_back.service;

import org.springframework.stereotype.Service;

@Service
public class TurnService {

    public String getTicket() {
        return "Nuevo ticket generado";
    }

    public String checkTicket() {
        return "Estado del ticket verificado";
    }
}
