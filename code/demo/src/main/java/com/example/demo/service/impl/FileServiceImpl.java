package com.example.demo.service.impl;

import com.example.demo.DAO.DAO;
import com.example.demo.Model.*;
import com.example.demo.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileServiceImpl implements FileService {
    private final DAO dao;

    @Autowired
    public FileServiceImpl(DAO dao) {
        this.dao = dao;
    }

    @Override
    public File saveProject(String code, String name, User user, String exist, File file, String role) {
        return dao.saveProject(code, name, user, exist, file, role);
    }

    @Override
    public UserFile fetchCode(String name, Long userId, Long fileId) {
        return dao.fetchCode(name, userId, fileId);
    }

    @Override
    public List<File> fetchProjects(Long userId) {
        return dao.fetchProjects(userId);
    }

    @Override
    public boolean deleteProject(Long fileId) {
        return dao.deleteProject(fileId);
    }

    @Override
    public List<Collaborator> fetchCollaborators(Long userId) {
        return dao.fetchCollaborators(userId);
    }

    @Override
    public boolean removeCollaborator(Long collaboratorId, Long userId, Long fileId) {
        return dao.removeCollaborator(collaboratorId, userId, fileId);
    }

    @Override
    public List<Comment> fetchComments(Long fileId, Long userId) {
        return dao.fetchComments(fileId, userId);
    }
    @Override
    public List<Version> fetchVersions(Long fileId) {
        return dao.fetchVersions(fileId);
    }

    @Override
    public UserFile addFileToCollaborator(String code, String name, Long fileId, Long collaboratorId, Long userId) {
        return dao.addFileToCollaborator(code, name, fileId, collaboratorId, userId);
    }

}
