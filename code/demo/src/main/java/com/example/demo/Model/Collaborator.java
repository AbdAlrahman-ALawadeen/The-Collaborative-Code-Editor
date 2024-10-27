package com.example.demo.Model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Collaborator {

    @NotNull
    @NotEmpty
    Long collaboratorID;

    @NotNull
    @NotEmpty
    Long projectID;

    public Collaborator(Long collaboratorID, Long projectID) {
        this.collaboratorID = collaboratorID;
        this.projectID = projectID;
    }
}
