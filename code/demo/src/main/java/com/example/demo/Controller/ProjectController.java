package com.example.demo.Controller;

import com.example.demo.Model.*;
import com.example.demo.service.FileService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@Controller
@RequestMapping("/controller")
public class ProjectController {
    private final UserService userService;
    private final FileService fileService;
    private User user = null;
    private String fileName = null;
    private File file = null;
    private String role = null;
    private UserFile userFile = null;

    @Autowired
    public ProjectController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @GetMapping("/")
    public String showIndex() {
        return "index";
    }

    @GetMapping("/registration")
    public String showRegistrationPage() {
        return "registration";
    }

    @GetMapping("/index")
    public String showIndexPage() {
        return "index";
    }

    @GetMapping("/editor")
    public String showEditorPage(Model model) {
        model.addAttribute("userId", user.getID());
        return "editor";
    }

    @GetMapping("/github")
    public String authByGithub() {
        return "editor";
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> verifyInfo(@RequestParam String email,
                                                          @RequestParam String password,
                                                          HttpSession session) {
        this.user = userService.fetchUserData(email);
        if (this.user == null || !this.user.getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid username or password"));
        }

        session.setAttribute("userId", user.getID());
        return ResponseEntity.ok(Map.of("userId", this.user.getID(), "message", "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestParam String username,
                                                            @RequestParam String email,
                                                            @RequestParam String password) {
        String resultMessage = userService.registerUser(username, email, password);

        Map<String, String> response = new HashMap<>();
        if (resultMessage.equals("Registration successful.")) {
            response.put("message", "Registration successful.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", resultMessage);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/runCode")
    public ResponseEntity<Map<String, String>> runCode(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        String language = payload.get("language");
        String userInput = payload.get("userInput");

        Path codePath;
        Path dockerPath;

        switch (language.toLowerCase()) {
            case "python":
                codePath = Paths.get("/app/src/main/java/dockerfiles/python/code.py");
                dockerPath = Paths.get("/app/src/main/java/dockerfiles/python");
                break;
            case "cpp":
                codePath = Paths.get("/app/src/main/java/dockerfiles/cpp/code.cpp");
                dockerPath = Paths.get("/app/src/main/java/dockerfiles/cpp");
                break;
            case "java":
                codePath = Paths.get("/app/src/main/java/dockerfiles/java/code.java");
                dockerPath = Paths.get("/app/src/main/java/dockerfiles/java");
                break;
            default:
                return ResponseEntity.badRequest().body(Map.of("error", "Unsupported language"));
        }

        try {
            Files.createDirectories(codePath.getParent());
            Files.writeString(codePath, code);
            Path inputPath = dockerPath.resolve("/app/src/main/java/dockerfiles/" + language + "/input.txt");
            Files.writeString(inputPath, userInput);

            if (!Files.exists(dockerPath.resolve("Dockerfile"))) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Dockerfile not found at: " + dockerPath.resolve("Dockerfile")));
            }

            ProcessBuilder buildProcess = new ProcessBuilder(
                    "docker", "build", "-f", dockerPath.resolve("Dockerfile").toString(), "-t", "code-runner", dockerPath.toString());
            Process build = buildProcess.start();

            String buildOutput = new BufferedReader(new InputStreamReader(build.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
            String buildError = new BufferedReader(new InputStreamReader(build.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));

            build.waitFor();

            if (build.exitValue() != 0) {
                StringBuilder formattedError = new StringBuilder();
                String[] lines = buildError.split("\n");

                boolean flag = false;
                for (String line : lines) {
                    if (line.contains("error:") || line.contains("warning:")) {
                        flag = true;
                        formattedError.append(line.trim()).append("\n");
                    }
                    if (flag && !line.contains("exit code:")) {
                        formattedError.append(line.trim()).append("\n");
                    } else if (line.contains("exit code:")) {
                        flag = false;
                    }
                }

                buildError = !formattedError.isEmpty() ? formattedError.toString().trim() : "No specific error message found.";

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", buildError));
            }

            ProcessBuilder runProcess = new ProcessBuilder(
                    "docker", "run", "--rm", "code-runner");
            Process run = runProcess.start();

            String result = new BufferedReader(new InputStreamReader(run.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            String errorOutput = new BufferedReader(new InputStreamReader(run.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));
            run.waitFor(60, TimeUnit.SECONDS);

            if (run.exitValue() != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", errorOutput));
            }

            return ResponseEntity.ok(Map.of("result", result));

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error executing the code: " + e.getMessage()));
        }
    }

    @PostMapping("/checkFileExistence")
    public ResponseEntity<Map<String, Object>> checkFileExistence(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (fileName != null) {
            String role = (this.userFile == null) ? "" : this.userFile.getRole();
            return ResponseEntity.ok(Map.of("fileExists", true, "fileName", fileName, "role", role));
        } else {
            return ResponseEntity.ok(Map.of("fileExists", false));
        }
    }

    @PostMapping("/saveCode")
    public ResponseEntity<Map<String, Object>> saveCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String name = request.get("fileName");
        String exist = request.get("exist");
        this.role = request.get("role");

        try {
            File response = fileService.saveProject(code, name, user, exist, this.file, this.role);
            String result;
            if (response != null) {
                result = "Project saved successfully";
                this.file = response;
                this.fileName = name;
                return ResponseEntity.ok(Map.of("message", result, "id", file.getId()));
            } else {
                result = "Project save/update failed";
                return ResponseEntity.ok(Map.of("message", result, "id", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error saving the code: " + e.getMessage()));
        }
    }

    @PostMapping("/sendComment")
    public ResponseEntity<Map<String, Object>> sendComment(@RequestBody Map<String, String> request) {
        String comment = request.get("text");
        Long id = Long.parseLong(request.get("fileID"));

        try {
            boolean result = userService.sendComment(comment, id, user.getID());
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error saving the code: " + e.getMessage()));
        }
    }

    @PostMapping("/fetchProjects")
    public ResponseEntity<List<File>> fetchProjects() {
        List<File> files = fileService.fetchProjects(user.getID());

        if (files != null && !files.isEmpty()) {
            return ResponseEntity.ok(files);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/fetchCode")
    public ResponseEntity<Map<String, Object>> fetchCode(@RequestBody Map<String, String> request) {
        String name = request.get("projectName");
        Long file_id = Long.valueOf(request.get("fileID"));
        try {
            UserFile userFile = fileService.fetchCode(name, user.getID(), file_id);
            this.userFile = userFile;
            this.file = new File(name, userFile.getContent(), userFile.getFileID());
            String result = userFile.getContent();
            this.fileName = name;
            return ResponseEntity.ok(Map.of("message", result, "id", userFile.getFileID(), "role", userFile.getRole()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error Can't fetch code : " + e.getMessage()));
        }
    }

    @PostMapping("/fetchVersions")
    public ResponseEntity<List<Version>> fetchVersions() {
        List<Version> versions = fileService.fetchVersions(file.getId());

        if (versions != null && !versions.isEmpty()) {
            return ResponseEntity.ok(versions);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/fetchComments")
    public ResponseEntity<List<Comment>> fetchComments(@RequestBody Map<String, String> request) {
        List<Comment> comments = fileService.fetchComments(this.file.getId(), user.getID());

        if (comments != null && !comments.isEmpty()) {
            return ResponseEntity.ok(comments);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/userExist")
    public ResponseEntity<Map<String, Object>> userExist(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Long id = userService.checkUserExist(email);
        if (id != null) {
            return ResponseEntity.ok(Map.of("collaboratorID", id));
        } else {
            return ResponseEntity.ok(Map.of("collaboratorID", false));
        }
    }

    @PostMapping("/addFileToCollaborator")
    public ResponseEntity<Map<String, Object>> addFileToCollaborator(@RequestBody Map<String, String> request) {
        String name = request.get("projectName");
        String code = request.get("code");
        Long fileID = Long.valueOf(request.get("fileID"));
        Long collaboratorID = Long.valueOf(request.get("collaboratorID"));
        try {
            UserFile response = fileService.addFileToCollaborator(code, name, fileID, collaboratorID, user.getID());
            String result;
            if (response != null) {
                result = "Project add Successfully to Collaborator";
            } else {
                result = "Add failed";
            }
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error: Can't add collaborator " + e.getMessage()));
        }
    }

    @PostMapping("/createProject")
    public ResponseEntity<String> createProject() {
        this.fileName = null;
        this.file = null;
        this.role = null;
        this.userFile = null;
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/deleteProject")
    public ResponseEntity<Map<String, Object>> deleteProject(@RequestBody Map<String, String> request) {
        Long fileID = Long.valueOf(request.get("fileID"));
        try {
            boolean response = fileService.deleteProject(fileID);
            String result;
            if (response) {
                result = "Project deleted successfully";
                this.fileName = null;
                this.userFile = null;
                this.file = null;
                this.role = null;
            } else {
                result = "Project deleted failed";
            }
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error: Can't delete project " + e.getMessage()));
        }
    }

    @PostMapping("/fetchCollaborators")
    public ResponseEntity<List<Collaborator>> fetchCollaborators(@RequestBody Map<String, String> request) {
        List<Collaborator> collaborators = fileService.fetchCollaborators(user.getID());
        if (collaborators != null && !collaborators.isEmpty()) {
            return ResponseEntity.ok(collaborators);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/removeCollaborator")
    public ResponseEntity<Map<String, Object>> removeCollaborator(@RequestBody Map<String, String> request) {
        Long collaboratorID = Long.valueOf(request.get("id"));
        try {
            boolean response = fileService.removeCollaborator(collaboratorID, user.getID(), file.getId());
            String result;
            if (response) {
                result = "Collaborator with ID: " + collaboratorID + " deleted successfully";
            } else {
                result = "Collaborator deleted failed";
            }
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error: Can't remove collaborator " + e.getMessage()));
        }
    }

    @PostMapping("/signOut")
    public ResponseEntity<String> signOut() {
        this.file = null;
        this.user = null;
        this.fileName = null;
        this.role = null;
        this.userFile = null;
        return ResponseEntity.ok("ok");
    }
}
