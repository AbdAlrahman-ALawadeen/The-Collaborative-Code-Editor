package com.example.demo.Model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@SuppressWarnings("ALL")
public class Comment {

    @NotNull
    @NotEmpty
    String comment;

    @NotNull
    @NotEmpty
    Long id;

    @NotNull
    @NotEmpty
    Long fileID;

    @NotNull
    @NotEmpty
    Long userID;

    public Comment(Long id, Long fileID, Long userID, String comment) {
        this.id = id;
        this.comment = comment;
        this.fileID = fileID;
        this.userID = userID;
    }


    public @NotNull @NotEmpty String getComment() {
        return comment;
    }

    public void setComment(@NotNull @NotEmpty String comment) {
        this.comment = comment;
    }

    public @NotNull @NotEmpty Long getId() {
        return id;
    }

    public void setId(@NotNull @NotEmpty Long id) {
        this.id = id;
    }

    public @NotNull @NotEmpty Long getFileID() {
        return fileID;
    }

    public void setFileID(@NotNull @NotEmpty Long fileID) {
        this.fileID = fileID;
    }

    public @NotNull @NotEmpty Long getUserID() {
        return userID;
    }

    public void setUserID(@NotNull @NotEmpty Long userID) {
        this.userID = userID;
    }
}
