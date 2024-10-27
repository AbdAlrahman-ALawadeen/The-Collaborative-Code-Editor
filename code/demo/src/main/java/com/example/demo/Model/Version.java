package com.example.demo.Model;

import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Setter
@Getter
public class Version {
    private Long id;
    private Long userID;
    private Long fileID;
    private String content;
    private Timestamp lastUpdated;

    public Version(Long id, Long userID, Long fileID, String content, Timestamp lastUpdated) {
        this.id = id;
        this.userID = userID;
        this.fileID = fileID;
        this.content = content;
        this.lastUpdated = lastUpdated;
    }

}
