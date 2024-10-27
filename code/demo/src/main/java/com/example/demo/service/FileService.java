package com.example.demo.service;

import com.example.demo.Model.*;

import java.util.List;

public interface FileService {
    File saveProject(String code, String name, User user, String exist, File file, String role);
    UserFile fetchCode(String name, Long userId, Long fileId);
    List<File> fetchProjects(Long userId);
    boolean deleteProject(Long fileId);
    List<Collaborator> fetchCollaborators(Long userId);
    boolean removeCollaborator(Long collaboratorId, Long userId, Long fileId);
    List<Comment> fetchComments(Long fileId, Long userId);
    List<Version> fetchVersions(Long fileId);
    UserFile addFileToCollaborator(String code, String name, Long fileId, Long collaboratorId, Long userId);
}
