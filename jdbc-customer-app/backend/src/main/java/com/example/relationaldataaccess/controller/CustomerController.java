package com.example.relationaldataaccess.controller;

import com.example.relationaldataaccess.model.Customer;
import com.example.relationaldataaccess.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:5173")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        log.info("GET /api/customers - Get all customers");
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Customer>> searchByFirstName(@RequestParam String firstName) {
        log.info("GET /api/customers/search?firstName={}", firstName);
        List<Customer> customers = customerService.searchByFirstName(firstName);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        log.info("GET /api/customers/{}", id);
        return customerService.getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Map<String, String> request) {
        String firstName = request.get("firstName");
        String lastName = request.get("lastName");
        
        log.info("POST /api/customers - Creating: {} {}", firstName, lastName);
        
        Customer customer = customerService.createCustomer(firstName, lastName);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        String firstName = request.get("firstName");
        String lastName = request.get("lastName");
        
        log.info("PUT /api/customers/{} - Updating: {} {}", id, firstName, lastName);
        
        return customerService.updateCustomer(id, firstName, lastName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("DELETE /api/customers/{}", id);
        
        boolean deleted = customerService.deleteCustomer(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCount() {
        log.info("GET /api/customers/count");
        int count = customerService.getCustomerCount();
        return ResponseEntity.ok(Map.of("count", count));
    }
}