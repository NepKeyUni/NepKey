package com.nepkey.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AssignmentDTO {
    private Long id;
    private String name;
    @JsonProperty("due_at")
    private String dueAt;

    // Getterek és setterek
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDueAt() { return dueAt; }
    public void setDueAt(String dueAt) { this.dueAt = dueAt; }
}