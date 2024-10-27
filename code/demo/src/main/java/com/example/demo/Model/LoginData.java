package com.example.demo.Model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@SuppressWarnings("ALL")
public class LoginData {
    @Email
    @NotEmpty
    @NotNull
    String email;

    @NotNull
    @NotEmpty
    String password;

    public @Email @NotEmpty @NotNull String getEmail() {
        return email;
    }

    public void setEmail(@Email @NotEmpty @NotNull String email) {
        this.email = email;
    }

    public @NotNull @NotEmpty String getPassword() {
        return password;
    }

    public void setPassword(@NotNull @NotEmpty String password) {
        this.password = password;
    }


}
