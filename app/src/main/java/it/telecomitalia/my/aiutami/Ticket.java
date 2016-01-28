package it.telecomitalia.my.aiutami;

import org.w3c.dom.Node;

public class Ticket {

    private String DataOraInizioSegnalazione;
    private String nomeClienteRagSoc;

    public Ticket(Node xml){

        DataOraInizioSegnalazione = "a";
        nomeClienteRagSoc = "b";

    }

    public String getDataOraInizioSegnalazione(){
        return DataOraInizioSegnalazione;
    }

    public String getNomeClienteRagSoc(){
        return nomeClienteRagSoc;
    }
}
