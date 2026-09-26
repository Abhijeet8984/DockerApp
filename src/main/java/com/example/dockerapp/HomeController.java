package com.example.dockerapp;

import java.util.List;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final NameFileStore names;

    public HomeController(NameFileStore names) {
        this.names = names;
    }

    @GetMapping("/")
    public String home() {
        return "DockerApp is running";
    }

    @PostMapping(path = "/names", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> saveName(@RequestBody String name) throws IOException {
        if (name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        names.save(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(name);
    }

    @GetMapping("/names")
    public List<String> getNames() throws IOException {
        return names.getAll();
    }
}

