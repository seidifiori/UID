package org.example.ProgettoUIDFinal.model;

//modello per l'organizzazione delle quest(task generabili dall'utente)
public class QuestModel {
    private String title;
    private String description;
    private int difficulty;

    public QuestModel(String title, String description, int difficulty) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getDifficulty() { return difficulty; }
}

