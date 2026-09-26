package com.example.dockerapp;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.data-file=target/test-names.jsonl")
@AutoConfigureMockMvc
class DockerAppApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @BeforeEach
    void clearSavedNames() throws Exception {
        Files.deleteIfExists(Path.of("target/test-names.jsonl"));
    }

    @Test
    void savesNameAndReturnsItFromGetRequest() throws Exception {
        mockMvc.perform(post("/names")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Ada"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/names"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Ada"));
    }
}
