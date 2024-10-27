package com.example.demo.DAO;

import com.example.demo.Model.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
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

    public UserFile fetchCode(String name, Long id, Long file_id){
        try {
            String sql = "SELECT user_id, file_id, content, role FROM USER_FILE WHERE user_id = ? and file_id = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{id, file_id}, (rs, rowNum) -> {
                UserFile userFile = new UserFile(rs.getLong("user_id"), rs.getLong("file_id"), rs.getString("content"), rs.getString("role"));
                return userFile;
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

    public List<File> fetchProjects(@NotNull @NotEmpty Long id) {
        try {
            String sql = "SELECT f.name, f.content, f.id " +
                    "FROM FILE f " +
                    "JOIN user_file uf ON f.id = uf.file_id " +
                    "WHERE uf.user_id = ?";


            return jdbcTemplate.query(sql, new Object[]{id}, (rs, rowNum) -> {
                return new File(
                        rs.getString("name"),
                        rs.getString("content"),
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

    public File saveProject(@NotNull @NotEmpty String code, @NotNull @NotEmpty String name, @NotNull @NotEmpty User user, String exist, File file, String role) {

        if(exist.equals("true")){
            try{
                jdbcTemplate.update("INSERT INTO VERSIONS (user_id, file_id, content, language) VALUES(?, ?, ?, ?)", user.getID(), file.getId(), code, "");
                int temp = jdbcTemplate.update("UPDATE FILE SET content = ? WHERE name = ?", code, name);
                jdbcTemplate.update("UPDATE USER_FILE SET CONTENT = ? WHERE user_id = ? and file_id = ?", code, user.getID(), file.getId());

                if(temp <= 0){
                    return null;
                }
                return file;
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
        else{
            try {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO FILE (name, content) VALUES(?, ?)",
                            new String[] {"id"}
                    );
                    ps.setString(1, name);
                    ps.setString(2, code);
                    return ps;
                }, keyHolder);

                Long generatedId = keyHolder.getKey().longValue();
                jdbcTemplate.update("INSERT INTO USER_FILE (user_id, file_id, content, role) VALUES(?, ?, ?, ?)", user.getID(), generatedId, code, role);

                jdbcTemplate.update("INSERT INTO VERSIONS (user_id, file_id, content, language) VALUES(?, ?, ?, ?)", user.getID(), generatedId, code, "");

                File f = new File(name, code, generatedId);
                user.addFile(f);

                return f;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public List<Version> fetchVersions(@NonNull @NotEmpty Long fileID) {
        try {
            String sql = "SELECT id, user_id, file_id, content, last_updated from VERSIONS WHERE file_id = ?" ;

            return jdbcTemplate.query(sql, new Object[]{fileID}, (rs, rowNum) -> {
                return new Version(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getLong("file_id"),
                        rs.getString("content"),
                        rs.getTimestamp("last_updated")
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

    public Long checkUserExist(@Email String email) {
        try {
            Long id = Long.valueOf(jdbcTemplate.queryForObject(
                    "SELECT id FROM USER WHERE email = ?",
                    new Object[]{email},
                    String.class
            ));
            return id;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public UserFile addFileToCollaborator(String code, String name, Long fileID, Long collaboratorID, @NotNull @NotEmpty Long userID) {
        try {
            try{
                jdbcTemplate.update("INSERT INTO USER_FILE (user_id, file_id, content, role) VALUES(?, ?, ?, ?)", collaboratorID, fileID, code, "Collaborator");
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }
            UserFile userFile = new UserFile(collaboratorID, fileID, code, "Collaborator");
            try{
                jdbcTemplate.update("INSERT INTO COLLABORATORS (admin_id, collaborator_id, project_id) VALUES(?, ?, ?)", userID, collaboratorID, fileID);
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }
            return userFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteProject(Long fileID) {
        try {
            Integer num = jdbcTemplate.update("DELETE FROM FILE WHERE id = ?", fileID);
            if(num == 0){
                return false;
            }
            return true;
        }
        catch (EmptyResultDataAccessException e) {
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Collaborator> fetchCollaborators(@NotNull @NotEmpty Long id) {
        try {
            String sql = "SELECT collaborator_id, project_id FROM COLLABORATORS WHERE admin_id = ?";


            return jdbcTemplate.query(sql, new Object[]{id}, (rs, rowNum) -> {
                return new Collaborator(
                        rs.getLong("collaborator_id"),
                        rs.getLong("project_id")
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

    public boolean removeCollaborator(Long collaboratorID, @NotNull @NotEmpty Long userID, @NotNull @NotEmpty Long fileID) {
        try {
            try {
                jdbcTemplate.update("DELETE FROM COLLABORATORS WHERE admin_id = ? and collaborator_id = ? and project_id = ?", userID, collaboratorID, fileID);
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
            jdbcTemplate.update("DELETE FROM USER_FILE WHERE user_id = ? and file_id = ?", collaboratorID, fileID);
            return true;
        }
        catch (EmptyResultDataAccessException e) {
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendComment(String comment, Long fileID, @NotNull @NotEmpty Long userID) {
        try{
            jdbcTemplate.update("INSERT INTO COMMENT (fileID, userID, comment) VALUES(?, ?, ?)", fileID, userID, comment);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public List<Comment> fetchComments(Long fileID, @NotNull @NotEmpty Long userID) {
        try {
            String sql = "SELECT id, fileID, userID, comment from Comment WHERE fileID = ?";

            return jdbcTemplate.query(sql, new Object[]{fileID}, (rs, rowNum) -> {
                return new Comment(
                        rs.getLong("id"),
                        rs.getLong("fileID"),
                        rs.getLong("userID"),
                        rs.getString("comment")
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
}