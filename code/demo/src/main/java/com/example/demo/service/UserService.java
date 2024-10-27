package com.example.demo.service;

import com.example.demo.Model.User;

public interface UserService {
    User fetchUserData(String email);
    String registerUser(String username, String email, String password);
    boolean sendComment(String comment, Long fileId, Long userId);
    Long checkUserExist(String email);
}
