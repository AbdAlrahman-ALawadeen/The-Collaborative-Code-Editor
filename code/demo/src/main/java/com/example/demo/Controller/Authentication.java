package com.example.demo.Controller;

import com.example.demo.DAO.DAO;
import com.example.demo.Model.LoginData;
import com.example.demo.Model.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.servlet.ModelAndView;


@RestController
@RequestMapping("/auth")
public class Authentication {

    private final DAO dao;

    @Autowired
    public Authentication(DAO dao){
        System.out.println("Enter Auth");
        this.dao = dao;
    }

    @PostMapping("login")
    public ModelAndView verifyInfo(@Valid @RequestBody @NotNull LoginData data, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView("login");

        if (bindingResult.hasErrors()) {
            modelAndView.addObject("errorMessage", "Invalid request: " + bindingResult.getAllErrors());
            return modelAndView;
        }

        User user = dao.fetchUserData(data.getEmail());

        if (user == null || !user.getPassword().equals(data.getPassword())) {
            modelAndView.addObject("errorMessage", "Invalid username or password");
            return modelAndView;
        }

        modelAndView.setViewName("redirect:/controller/editor");
        return modelAndView;
    }


    @PostMapping("register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody @NotNull User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: " + bindingResult.getAllErrors());
        }

        dao.registerUser(user.getUsername(), user.getEmail(), user.getPassword());

        return ResponseEntity.ok().body("Registered successfully");
    }

}
