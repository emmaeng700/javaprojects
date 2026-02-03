package com.example.relationaldataaccess.repository;

import com.example.relationaldataaccess.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepository {

    private static final Logger log = LoggerFactory.getLogger(CustomerRepository.class);
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Customer> customerRowMapper = (rs, rowNum) ->
            new Customer(
                    rs.getLong("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name")
            );

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void initializeDatabase() {
        log.info("Creating customers table");
        jdbcTemplate.execute("DROP TABLE IF EXISTS customers");
        jdbcTemplate.execute("CREATE TABLE customers(" +
                "id IDENTITY PRIMARY KEY, " +
                "first_name VARCHAR(255), " +
                "last_name VARCHAR(255))");
    }

    public int[] batchInsert(List<Object[]> customers) {
        log.info("Batch inserting {} customers", customers.size());
        return jdbcTemplate.batchUpdate(
                "INSERT INTO customers(first_name, last_name) VALUES (?,?)",
                customers
        );
    }

    public int insert(String firstName, String lastName) {
        log.info("Inserting customer: {} {}", firstName, lastName);
        return jdbcTemplate.update(
                "INSERT INTO customers(first_name, last_name) VALUES (?,?)",
                firstName, lastName
        );
    }

    public List<Customer> findAll() {
        log.debug("Finding all customers");
        return jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers",
                customerRowMapper
        );
    }

    public List<Customer> findByFirstName(String firstName) {
        log.debug("Finding customers by first name: {}", firstName);
        return jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers WHERE first_name = ?",
                customerRowMapper,
                firstName
        );
    }

    public Optional<Customer> findById(Long id) {
        log.debug("Finding customer by id: {}", id);
        List<Customer> customers = jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers WHERE id = ?",
                customerRowMapper,
                id
        );
        return customers.isEmpty() ? Optional.empty() : Optional.of(customers.get(0));
    }

    public int update(Long id, String firstName, String lastName) {
        log.info("Updating customer id: {}", id);
        return jdbcTemplate.update(
                "UPDATE customers SET first_name = ?, last_name = ? WHERE id = ?",
                firstName, lastName, id
        );
    }

    public int deleteById(Long id) {
        log.info("Deleting customer id: {}", id);
        return jdbcTemplate.update("DELETE FROM customers WHERE id = ?", id);
    }

    public int count() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class);
        return count != null ? count : 0;
    }
}