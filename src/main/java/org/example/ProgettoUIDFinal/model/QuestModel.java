package org.example.ProgettoUIDFinal.model;

public class QuestModel {
    private String titolo;
    private String descrizione;
    private int difficolta;

    public QuestModel(String titolo, String descrizione, int difficolta) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.difficolta = difficolta;
    }

    public String getTitolo() { return titolo; }
    public String getDescrizione() { return descrizione; }
    public int getDifficolta() { return difficolta; }
}

