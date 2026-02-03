package com.example.relationaldataaccess.service;

import com.example.relationaldataaccess.model.Customer;
import com.example.relationaldataaccess.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.initializeDatabase();
    }

    @Test
    void customerService_shouldBeInjected() {
        assertThat(customerService).isNotNull();
    }

    @Test
    void getAllCustomers_shouldReturnAllCustomers() {
        customerRepository.insert("Alice", "Smith");
        customerRepository.insert("Bob", "Johnson");
        
        List<Customer> customers = customerService.getAllCustomers();
        
        assertThat(customers).hasSize(2);
    }

    @Test
    void searchByFirstName_shouldReturnMatchingCustomers() {
        customerRepository.insert("Josh", "Bloch");
        customerRepository.insert("Josh", "Long");
        customerRepository.insert("John", "Doe");
        
        List<Customer> customers = customerService.searchByFirstName("Josh");
        
        assertThat(customers).hasSize(2);
        assertThat(customers).extracting(Customer::firstName)
                .containsOnly("Josh");
    }

    @Test
    void getCustomerById_shouldReturnCustomer() {
        customerRepository.insert("Alice", "Smith");
        List<Customer> all = customerRepository.findAll();
        Long id = all.get(0).id();
        
        Optional<Customer> customer = customerService.getCustomerById(id);
        
        assertThat(customer).isPresent();
        assertThat(customer.get().firstName()).isEqualTo("Alice");
    }

    @Test
    void createCustomer_shouldAddNewCustomer() {
        Customer customer = customerService.createCustomer("Charlie", "Brown");
        
        assertThat(customer).isNotNull();
        assertThat(customer.firstName()).isEqualTo("Charlie");
        assertThat(customer.lastName()).isEqualTo("Brown");
        assertThat(customerService.getCustomerCount()).isEqualTo(1);
    }

    @Test
    void updateCustomer_shouldModifyExistingCustomer() {
        customerRepository.insert("Alice", "Smith");
        List<Customer> all = customerRepository.findAll();
        Long id = all.get(0).id();
        
        Optional<Customer> updated = customerService.updateCustomer(id, "Alice", "Johnson");
        
        assertThat(updated).isPresent();
        assertThat(updated.get().lastName()).isEqualTo("Johnson");
    }

    @Test
    void updateCustomer_withInvalidId_shouldReturnEmpty() {
        Optional<Customer> updated = customerService.updateCustomer(999L, "Test", "User");
        
        assertThat(updated).isEmpty();
    }

    @Test
    void deleteCustomer_shouldRemoveCustomer() {
        customerRepository.insert("Alice", "Smith");
        List<Customer> all = customerRepository.findAll();
        Long id = all.get(0).id();
        
        boolean deleted = customerService.deleteCustomer(id);
        
        assertThat(deleted).isTrue();
        assertThat(customerService.getCustomerCount()).isEqualTo(0);
    }

    @Test
    void deleteCustomer_withInvalidId_shouldReturnFalse() {
        boolean deleted = customerService.deleteCustomer(999L);
        
        assertThat(deleted).isFalse();
    }

    @Test
    void getCustomerCount_shouldReturnCorrectCount() {
        assertThat(customerService.getCustomerCount()).isEqualTo(0);
        
        customerService.createCustomer("Alice", "Smith");
        assertThat(customerService.getCustomerCount()).isEqualTo(1);
        
        customerService.createCustomer("Bob", "Johnson");
        assertThat(customerService.getCustomerCount()).isEqualTo(2);
    }
}