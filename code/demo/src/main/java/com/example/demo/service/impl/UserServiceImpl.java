package com.example.demo.service.impl;

import com.example.demo.DAO.DAO;
import com.example.demo.Model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final DAO dao;
    @Autowired
    public UserServiceImpl(DAO dao) {
        this.dao = dao;
    }
    @Override
    public User fetchUserData(String email) {
        return dao.fetchUserData(email);
    }
    @Override
    public String registerUser(String username, String email, String password) {
        return dao.registerUser(username, email, password);
    }
    @Override
    public boolean sendComment(String comment, Long fileId, Long userId) {
        return dao.sendComment(comment, fileId, userId);
    }
    @Override
    public Long checkUserExist(String email) {
        return dao.checkUserExist(email);
    }
}
