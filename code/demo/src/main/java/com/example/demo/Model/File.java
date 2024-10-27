package com.example.demo.Model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
public class File {
    @NotNull
    @NotEmpty
    String fileName;

    @NotNull
    @NotEmpty
    String content;

    @NotNull
    @NotEmpty
    Long id;



    public File(@NonNull String fileName, @NonNull String fileContent, long id) {
        this.fileName = fileName;
        this.content = fileContent;
        this.id = id;
    }

}
