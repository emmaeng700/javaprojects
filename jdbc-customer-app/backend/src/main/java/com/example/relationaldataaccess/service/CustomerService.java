package com.example.relationaldataaccess.service;

import com.example.relationaldataaccess.model.Customer;
import com.example.relationaldataaccess.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        log.debug("Service: Getting all customers");
        return customerRepository.findAll();
    }

    public List<Customer> searchByFirstName(String firstName) {
        log.debug("Service: Searching customers by first name: {}", firstName);
        return customerRepository.findByFirstName(firstName);
    }

    public Optional<Customer> getCustomerById(Long id) {
        log.debug("Service: Getting customer by id: {}", id);
        return customerRepository.findById(id);
    }

    public Customer createCustomer(String firstName, String lastName) {
        log.info("Service: Creating customer: {} {}", firstName, lastName);
        customerRepository.insert(firstName, lastName);
        
        // Return the newly created customer
        List<Customer> customers = customerRepository.findByFirstName(firstName);
        return customers.stream()
                .filter(c -> c.lastName().equals(lastName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to create customer"));
    }

    public Optional<Customer> updateCustomer(Long id, String firstName, String lastName) {
        log.info("Service: Updating customer id: {}", id);
        int updated = customerRepository.update(id, firstName, lastName);
        
        if (updated > 0) {
            return customerRepository.findById(id);
        }
        return Optional.empty();
    }

    public boolean deleteCustomer(Long id) {
        log.info("Service: Deleting customer id: {}", id);
        int deleted = customerRepository.deleteById(id);
        return deleted > 0;
    }

    public int getCustomerCount() {
        return customerRepository.count();
    }
}