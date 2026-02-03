package com.example.relationaldataaccess.integration;

import com.example.relationaldataaccess.model.Customer;
import com.example.relationaldataaccess.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/customers";
        customerRepository.initializeDatabase();
    }

    @Test
    void contextLoads() {
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void fullCrudWorkflow_shouldWork() {
        // CREATE
        Map<String, String> newCustomer = Map.of(
                "firstName", "Alice",
                "lastName", "Wonderland"
        );
        
        ResponseEntity<Customer> createResponse = restTemplate.postForEntity(
                baseUrl,
                newCustomer,
                Customer.class
        );
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().firstName()).isEqualTo("Alice");
        
        Long customerId = createResponse.getBody().id();
        
        // READ
        ResponseEntity<Customer> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + customerId,
                Customer.class
        );
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().firstName()).isEqualTo("Alice");
        
        // UPDATE
        Map<String, String> updateCustomer = Map.of(
                "firstName", "Alice",
                "lastName", "InChains"
        );
        
        restTemplate.put(baseUrl + "/" + customerId, updateCustomer);
        
        ResponseEntity<Customer> updatedResponse = restTemplate.getForEntity(
                baseUrl + "/" + customerId,
                Customer.class
        );
        
        assertThat(updatedResponse.getBody()).isNotNull();
        assertThat(updatedResponse.getBody().lastName()).isEqualTo("InChains");
        
        // DELETE
        restTemplate.delete(baseUrl + "/" + customerId);
        
        ResponseEntity<Customer> deletedResponse = restTemplate.getForEntity(
                baseUrl + "/" + customerId,
                Customer.class
        );
        
        assertThat(deletedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllCustomers_shouldReturnList() {
        customerRepository.insert("John", "Doe");
        customerRepository.insert("Jane", "Doe");
        
        ResponseEntity<List> response = restTemplate.getForEntity(baseUrl, List.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void searchByFirstName_shouldReturnMatches() {
        customerRepository.insert("Josh", "Bloch");
        customerRepository.insert("Josh", "Long");
        customerRepository.insert("John", "Doe");
        
        ResponseEntity<List> response = restTemplate.getForEntity(
                baseUrl + "/search?firstName=Josh",
                List.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }
}