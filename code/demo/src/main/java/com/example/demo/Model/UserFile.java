package com.example.demo.Model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserFile {
    @NotEmpty
    @NotNull
    Long userID;

    @NotEmpty
    @NotNull
    Long fileID;

    @NotEmpty
    @NotNull
    String content;

    @NotEmpty
    @NotNull
    String role;

    public UserFile(Long userID, Long fileID, String content, String role) {
        this.userID = userID;
        this.fileID = fileID;
        this.content = content;
        this.role = role;
    }
}
