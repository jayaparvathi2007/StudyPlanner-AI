package com.studyplanner.model;

import java.util.List;

public class QuizQuestion {
    private int id;
    private String question;
    private List<String> options;
    private int correctIndex;
    private String explanation;

    public QuizQuestion() {}

    public QuizQuestion(int id, String question, List<String> options, int correctIndex, String explanation) {
        this.id = id;
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
