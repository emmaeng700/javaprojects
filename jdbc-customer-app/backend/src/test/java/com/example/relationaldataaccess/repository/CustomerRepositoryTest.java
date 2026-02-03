package com.example.relationaldataaccess.repository;

import com.example.relationaldataaccess.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        customerRepository.initializeDatabase();
    }

    @Test
    void customerRepository_shouldBeInjected() {
        assertThat(customerRepository).isNotNull();
    }

    @Test
    void initializeDatabase_shouldCreateTable() {
        customerRepository.initializeDatabase();
        
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CUSTOMERS'",
                Integer.class
        );
        
        assertThat(count).isEqualTo(1);
    }

    @Test
    void insert_shouldAddCustomer() {
        int result = customerRepository.insert("Alice", "Smith");
        
        assertThat(result).isEqualTo(1);
        assertThat(customerRepository.count()).isEqualTo(1);
    }

    @Test
    void batchInsert_shouldAddMultipleCustomers() {
        List<Object[]> customers = List.of(
                new Object[]{"John", "Doe"},
                new Object[]{"Jane", "Doe"},
                new Object[]{"Bob", "Smith"}
        );
        
        int[] results = customerRepository.batchInsert(customers);
        
        assertThat(results).hasSize(3);
        assertThat(customerRepository.count()).isEqualTo(3);
    }

    @Test
    void findAll_shouldReturnAllCustomers() {
        customerRepository.insert("Alice", "Smith");
        customerRepository.insert("Bob", "Johnson");
        customerRepository.insert("Charlie", "Brown");
        
        List<Customer> customers = customerRepository.findAll();
        
        assertThat(customers).hasSize(3);
        assertThat(customers).extracting(Customer::firstName)
                .containsExactlyInAnyOrder("Alice", "Bob", "Charlie");
    }

    @Test
    void findAll_withEmptyTable_shouldReturnEmptyList() {
        List<Customer> customers = customerRepository.findAll();
        
        assertThat(customers).isEmpty();
    }

    @Test
    void findByFirstName_shouldReturnMatchingCustomers() {
        customerRepository.insert("Josh", "Bloch");
        customerRepository.insert("Josh", "Long");
        customerRepository.insert("John", "Doe");
        
        List<Customer> joshCustomers = customerRepository.findByFirstName("Josh");
        
        assertThat(joshCustomers).hasSize(2);
        assertThat(joshCustomers).extracting(Customer::firstName)
                .containsOnly("Josh");
        assertThat(joshCustomers).extracting(Customer::lastName)
                .containsExactlyInAnyOrder("Bloch", "Long");
    }

    @Test
    void findByFirstName_withNoMatches_shouldReturnEmptyList() {
        customerRepository.insert("Alice", "Smith");
        
        List<Customer> customers = customerRepository.findByFirstName("Bob");
        
        assertThat(customers).isEmpty();
    }

    @Test
    void findById_shouldReturnCustomer() {
        customerRepository.insert("Alice", "Smith");
        
        List<Customer> allCustomers = customerRepository.findAll();
        Long id = allCustomers.get(0).id();
        
        Optional<Customer> customer = customerRepository.findById(id);
        
        assertThat(customer).isPresent();
        assertThat(customer.get().firstName()).isEqualTo("Alice");
        assertThat(customer.get().lastName()).isEqualTo("Smith");
    }

    @Test
    void findById_withInvalidId_shouldReturnEmpty() {
        Optional<Customer> customer = customerRepository.findById(999L);
        
        assertThat(customer).isEmpty();
    }

    @Test
    void update_shouldModifyCustomer() {
        customerRepository.insert("Alice", "Smith");
        
        List<Customer> customers = customerRepository.findAll();
        Long id = customers.get(0).id();
        
        int updated = customerRepository.update(id, "Alice", "Johnson");
        
        assertThat(updated).isEqualTo(1);
        
        Optional<Customer> updatedCustomer = customerRepository.findById(id);
        assertThat(updatedCustomer).isPresent();
        assertThat(updatedCustomer.get().lastName()).isEqualTo("Johnson");
    }

    @Test
    void update_withInvalidId_shouldReturnZero() {
        int updated = customerRepository.update(999L, "Test", "User");
        
        assertThat(updated).isEqualTo(0);
    }

    @Test
    void deleteById_shouldRemoveCustomer() {
        customerRepository.insert("Alice", "Smith");
        
        List<Customer> customers = customerRepository.findAll();
        Long id = customers.get(0).id();
        
        int deleted = customerRepository.deleteById(id);
        
        assertThat(deleted).isEqualTo(1);
        assertThat(customerRepository.count()).isEqualTo(0);
    }

    @Test
    void deleteById_withInvalidId_shouldReturnZero() {
        int deleted = customerRepository.deleteById(999L);
        
        assertThat(deleted).isEqualTo(0);
    }

    @Test
    void count_shouldReturnCorrectNumber() {
        assertThat(customerRepository.count()).isEqualTo(0);
        
        customerRepository.insert("Alice", "Smith");
        assertThat(customerRepository.count()).isEqualTo(1);
        
        customerRepository.insert("Bob", "Johnson");
        assertThat(customerRepository.count()).isEqualTo(2);
    }
}