package com.example.validatingforminput.service;

import com.example.validatingforminput.model.Registration;
import com.example.validatingforminput.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class RegistrationService {

    private final RegistrationRepository repository;

    public RegistrationService(RegistrationRepository repository) {
        this.repository = repository;
    }

    public Registration save(Registration registration) {
        return repository.save(registration);
    }

    @Transactional(readOnly = true)
    public Optional<Registration> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Registration> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Registration> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        return repository.search(query);
    }

    public Registration update(Long id, Registration updated) {
        Registration existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Registration not found: " + id));
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setAge(updated.getAge());
        existing.setPhone(updated.getPhone());
        return repository.save(existing);
    }

    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    @Transactional(readOnly = true)
    public double averageAge() {
        Double avg = repository.averageAge();
        return avg != null ? avg : 0.0;
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public boolean emailExistsExcluding(String email, Long excludeId) {
        return repository.existsByEmailIgnoreCaseAndIdNot(email, excludeId);
    }

    @Transactional(readOnly = true)
    public List<Registration> recentRegistrations(int limit) {
        return repository.findTop5ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> ageDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("18-25", repository.countByAgeBetween(18, 25));
        distribution.put("26-35", repository.countByAgeBetween(26, 35));
        distribution.put("36-50", repository.countByAgeBetween(36, 50));
        distribution.put("51+", repository.countByAgeBetween(51, 200));
        return distribution;
    }
}
