package com.example.validatingforminput.repository;

import com.example.validatingforminput.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    List<Registration> findAllByOrderByCreatedAtDesc();

    List<Registration> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT r FROM Registration r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "r.phone LIKE CONCAT('%', :query, '%') " +
           "ORDER BY r.createdAt DESC")
    List<Registration> search(@Param("query") String query);

    @Query("SELECT AVG(r.age) FROM Registration r")
    Double averageAge();

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.age BETWEEN :min AND :max")
    long countByAgeBetween(@Param("min") int min, @Param("max") int max);
}
