package com.example.demo.Controller;

import com.example.demo.DAO.DAO;
import com.example.demo.Model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@Controller
@RequestMapping("/controller")
public class ProjectController {
    private final DAO dao;

    public ProjectController(DAO dao) {
        this.dao = dao;
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
    public String showEditorPage() {
        return "editor";
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> verifyInfo(@RequestParam String email,
                                                          @RequestParam String password,
                                                          HttpSession session) {
        User user = dao.fetchUserData(email);

        if (user == null || !user.getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid username or password"));
        }

        session.setAttribute("userId", user.getID());
        return ResponseEntity.ok(Map.of("userId", user.getID(), "message", "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestParam String username,
                                                            @RequestParam String email,
                                                            @RequestParam String password) {
        String resultMessage = dao.registerUser(username, email, password);

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
        System.out.println("Current working directory: " + Paths.get("").toAbsolutePath());

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

            if (Files.notExists(codePath)) {
                Files.createFile(codePath);
                System.out.println("File created: " + codePath);
            }

            Files.writeString(codePath, code);
            System.out.println("Code written to: " + codePath);

            if (!Files.exists(dockerPath.resolve("Dockerfile"))) {
                System.out.println("Dockerfile not found");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Dockerfile not found at: " + dockerPath.resolve("Dockerfile")));
            }

            ProcessBuilder buildProcess = new ProcessBuilder(
                    "docker", "build", "-f", dockerPath.resolve("Dockerfile").toString(), "-t", "code-runner", dockerPath.toString());
            Process build = buildProcess.start();

            String buildError = new BufferedReader(new InputStreamReader(build.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));
            build.waitFor(60, TimeUnit.SECONDS);

            if (build.exitValue() != 0) {
                System.out.println("Failed to build Docker image: " + buildError);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to build Docker image", "details", buildError));
            }
            System.out.println("Docker image built successfully.");

            ProcessBuilder runProcess = new ProcessBuilder(
                    "docker", "run", "--rm", "code-runner");
            Process run = runProcess.start();
            String result = new BufferedReader(new InputStreamReader(run.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
            run.waitFor(60, TimeUnit.SECONDS);

            if (run.exitValue() != 0) {
                System.out.println("Failed to run Docker container.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Error running the code"));
            }

            System.out.println("Docker container ran successfully.");
            return ResponseEntity.ok(Map.of("result", result));

        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error executing the code: " + e.getMessage()));
        }
    }

}
