package com.example.relationaldataaccess.controller;

import com.example.relationaldataaccess.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.initializeDatabase();
    }

    @Test
    void getAllCustomers_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllCustomers_shouldReturnCustomers() throws Exception {
        customerRepository.insert("Alice", "Smith");
        customerRepository.insert("Bob", "Johnson");
        
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", notNullValue()))
                .andExpect(jsonPath("$[1].firstName", notNullValue()));
    }

    @Test
    void searchByFirstName_shouldReturnMatchingCustomers() throws Exception {
        customerRepository.insert("Josh", "Bloch");
        customerRepository.insert("Josh", "Long");
        customerRepository.insert("John", "Doe");
        
        mockMvc.perform(get("/api/customers/search")
                        .param("firstName", "Josh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("Josh")))
                .andExpect(jsonPath("$[1].firstName", is("Josh")));
    }

    @Test
    void getCustomerById_shouldReturnCustomer() throws Exception {
        customerRepository.insert("Alice", "Smith");
        Long id = customerRepository.findAll().get(0).id();
        
        mockMvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.firstName", is("Alice")))
                .andExpect(jsonPath("$.lastName", is("Smith")));
    }

    @Test
    void getCustomerById_withInvalidId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCustomer_shouldAddNewCustomer() throws Exception {
        String customerJson = """
                {
                    "firstName": "Charlie",
                    "lastName": "Brown"
                }
                """;
        
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("Charlie")))
                .andExpect(jsonPath("$.lastName", is("Brown")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void updateCustomer_shouldModifyCustomer() throws Exception {
        customerRepository.insert("Alice", "Smith");
        Long id = customerRepository.findAll().get(0).id();
        
        String updateJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Johnson"
                }
                """;
        
        mockMvc.perform(put("/api/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Alice")))
                .andExpect(jsonPath("$.lastName", is("Johnson")));
    }

    @Test
    void updateCustomer_withInvalidId_shouldReturn404() throws Exception {
        String updateJson = """
                {
                    "firstName": "Test",
                    "lastName": "User"
                }
                """;
        
        mockMvc.perform(put("/api/customers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCustomer_shouldRemoveCustomer() throws Exception {
        customerRepository.insert("Alice", "Smith");
        Long id = customerRepository.findAll().get(0).id();
        
        mockMvc.perform(delete("/api/customers/{id}", id))
                .andExpect(status().isNoContent());
        
        // Verify deletion
        mockMvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCustomer_withInvalidId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/customers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCount_shouldReturnCorrectCount() throws Exception {
        customerRepository.insert("Alice", "Smith");
        customerRepository.insert("Bob", "Johnson");
        
        mockMvc.perform(get("/api/customers/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(2)));
    }
}