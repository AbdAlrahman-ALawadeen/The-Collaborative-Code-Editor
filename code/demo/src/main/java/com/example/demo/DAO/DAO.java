package com.example.demo.DAO;

import com.example.demo.Model.File;
import com.example.demo.Model.Project;
import com.example.demo.Model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.List;


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

    public Project fetchCode(String name){
        try {
            String sql = "SELECT name, content, user_id, id FROM FILE WHERE name = ?";

            return jdbcTemplate.queryForObject(sql, new Object[]{name}, (rs, rowNum) -> {
                Project project = new Project(rs.getString("name"), rs.getString("content"), rs.getLong("user_id"), rs.getLong("id"));
                return project;
            });
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Project> fetchProjects(@NotNull @NotEmpty Long id) {
        try {
            String sql = "SELECT name, content, user_id, id FROM FILE WHERE user_id = ?";

            return jdbcTemplate.query(sql, new Object[]{id}, (rs, rowNum) -> {
                return new Project(
                        rs.getString("name"),
                        rs.getString("content"),
                        rs.getLong("user_id"),
                        rs.getLong("id")
                );
            });
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String checkFileExistence(@NotNull @NotEmpty String code) {
        try {
            String fileName = jdbcTemplate.queryForObject(
                    "SELECT name FROM FILE WHERE content = ?",
                    new Object[]{code},
                    String.class
            );
            return fileName;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public String saveProject( @NotNull @NotEmpty String code,  @NotNull @NotEmpty String name, @NotNull @NotEmpty User user, String exist) {

        if(exist.equals("true")){
            try{
                int temp = jdbcTemplate.update("UPDATE FILE SET content = ? WHERE name = ?", code, name);
                if(temp <= 0){
                    System.out.println(temp);
                    return "Can't update project";
                }
                return "Project successfully updated";
            }
            catch (Exception e){
                e.printStackTrace();
                return "Can't update project";
            }
        }
        else{
            try {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO FILE (name, content, user_id) VALUES(?, ?, ?)",
                            new String[] {"id"}
                    );
                    ps.setString(1, name);
                    ps.setString(2, code);
                    ps.setLong(3, user.getID());
                    return ps;
                }, keyHolder);

                Long generatedId = keyHolder.getKey().longValue();

                File file = new File(name, code, generatedId, user.getID());
                Project project = new Project(name, code, user.getID(), generatedId);
                user.addProject(project);

                System.out.println("Project Successfully Saved with ID: " + generatedId);
                return "Project successfully saved with ID: " + generatedId;

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("An error occurred during saving project. Please try again.");
                return "An error occurred during saving project. Please try again.";
            }
        }
    }

}
