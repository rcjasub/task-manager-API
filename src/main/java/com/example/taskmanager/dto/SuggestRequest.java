package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class SuggestRequest {

    @NotBlank(message = "description must not be blank")
    private String description;

    public SuggestRequest() {}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}