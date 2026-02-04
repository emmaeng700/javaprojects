package com.example.uploadingfiles.integration;

import com.example.uploadingfiles.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileUploadIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StorageService storageService;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
        storageService.deleteAll();
        storageService.init();
    }

    @Test
    void contextLoads() {
        assertThat(restTemplate).isNotNull();
        assertThat(storageService).isNotNull();
    }

    @Test
    void listFiles_withNoFiles_shouldReturnEmptyList() {
        ResponseEntity<List> response = restTemplate.getForEntity(
                baseUrl + "/files",
                List.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getCount_withNoFiles_shouldReturnZero() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/count",
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("count")).isEqualTo(0);
    }

    @Test
    void downloadNonExistentFile_shouldReturn404() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/download/nonexistent.txt",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}