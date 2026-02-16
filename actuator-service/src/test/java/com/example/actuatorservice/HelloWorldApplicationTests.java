package com.example.actuatorservice;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"management.port=0"})
public class HelloWorldApplicationTests {

    @LocalServerPort
    private int port;

    @Value("${local.management.port}")
    private int mgt;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void shouldReturn200WhenSendingRequestToController() {
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> entity = this.testRestTemplate.getForEntity(
                "http://localhost:" + this.port + "/hello-world", Map.class);

        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnGreetingWithDefaultName() {
        ResponseEntity<Greeting> entity = this.testRestTemplate.getForEntity(
                "http://localhost:" + this.port + "/hello-world", Greeting.class);

        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(entity.getBody()).isNotNull();
        then(entity.getBody().getContent()).isEqualTo("Hello, Stranger!");
    }

    @Test
    public void shouldReturnGreetingWithCustomName() {
        ResponseEntity<Greeting> entity = this.testRestTemplate.getForEntity(
                "http://localhost:" + this.port + "/hello-world?name=Spring", Greeting.class);

        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(entity.getBody()).isNotNull();
        then(entity.getBody().getContent()).isEqualTo("Hello, Spring!");
    }

    @Test
    public void shouldIncrementIdOnEachRequest() {
        ResponseEntity<Greeting> first = this.testRestTemplate.getForEntity(
                "http://localhost:" + this.port + "/hello-world", Greeting.class);
        ResponseEntity<Greeting> second = this.testRestTemplate.getForEntity(
                "http://localhost:" + this.port + "/hello-world", Greeting.class);

        then(second.getBody().getId()).isGreaterThan(first.getBody().getId());
    }

    @Test
    public void shouldReturn200WhenSendingRequestToManagementEndpoint() {
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> entity = this.testRestTemplate.getForEntity(
                "http://localhost:" + this.mgt + "/actuator", Map.class);
        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnHealthEndpoint() {
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> entity = this.testRestTemplate.getForEntity(
                "http://localhost:" + this.mgt + "/actuator/health", Map.class);

        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(entity.getBody().get("status")).isEqualTo("UP");
    }

    @Test
    public void shouldReturnGreetingStats() {
        // Send a greeting first
        this.testRestTemplate.getForEntity(
                "http://localhost:" + this.port + "/hello-world?name=TestUser", Greeting.class);

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> entity = this.testRestTemplate.getForEntity(
                "http://localhost:" + this.port + "/api/greetings/stats", Map.class);

        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(entity.getBody()).containsKey("totalGreetings");
        then(entity.getBody()).containsKey("uniqueNames");
        then(entity.getBody()).containsKey("topNames");
    }

    @Test
    public void shouldReturnGreetingHistory() {
        // Send a greeting first
        this.testRestTemplate.getForEntity(
                "http://localhost:" + this.port + "/hello-world?name=HistoryUser", Greeting.class);

        @SuppressWarnings("rawtypes")
        ResponseEntity<List> entity = this.testRestTemplate.getForEntity(
                "http://localhost:" + this.port + "/api/greetings/history", List.class);

        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(entity.getBody()).isNotEmpty();
    }

    @Test
    public void shouldReturnDashboardPage() {
        ResponseEntity<String> entity = this.testRestTemplate.getForEntity(
                "http://localhost:" + this.port + "/", String.class);

        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(entity.getBody()).contains("Service Dashboard");
    }

    @Test
    public void shouldReturnExplorerPage() {
        ResponseEntity<String> entity = this.testRestTemplate.getForEntity(
                "http://localhost:" + this.port + "/explorer", String.class);

        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(entity.getBody()).contains("API Explorer");
    }

    @Test
    public void shouldReturnEndpointsPage() {
        ResponseEntity<String> entity = this.testRestTemplate.getForEntity(
                "http://localhost:" + this.port + "/endpoints", String.class);

        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(entity.getBody()).contains("Actuator Endpoints");
    }
}
