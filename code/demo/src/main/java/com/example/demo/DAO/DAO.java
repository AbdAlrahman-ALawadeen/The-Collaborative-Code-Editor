package com.example.demo.DAO;

import com.example.demo.Model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


@SuppressWarnings("ALL")
@Service
@Configuration
public class DAO {

    private final JdbcTemplate jdbcTemplate;
    private static final String register = "";
    @Autowired
    public DAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public User fetchUserData(@Email @NotNull @NotEmpty String email) {
        try {
            String sql = "SELECT username, email, hashPassword, id FROM USER WHERE EMAIL = ?";

            return jdbcTemplate.queryForObject(sql, new Object[]{email}, (rs, rowNum) -> {
                User user = new User();
                user.setID(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("hashPassword"));
                return user;
            });
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public String registerUser(@NotNull @NotEmpty String username, @Email @NotNull @NotEmpty String email, @NotNull @NotEmpty String password) {
        Integer num = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USER WHERE EMAIL = ?", new Object[]{email}, Integer.class);

        if(num != null && num > 0) {
            return "User already exists with this email.";
        }
        System.out.println("Enter DAO");
        try {
            jdbcTemplate.update("INSERT INTO USER (username, email, hashPassword) VALUES(?, ?, ?)", username, email, password);
            return "Registration successful.";
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred during registration. Please try again.";
        }
    }


    public String getPassword(@Email @NotNull @NotEmpty String email){
        try {
            return jdbcTemplate.queryForObject("SELECT hashPassword FROM USER WHERE EMAIL = ?", new Object[]{email}, String.class);
        }
        catch (EmptyResultDataAccessException e){
            return null;
        }
    }
}
