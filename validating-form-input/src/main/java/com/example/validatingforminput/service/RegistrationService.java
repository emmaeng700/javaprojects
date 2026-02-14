package com.example.validatingforminput.service;

import com.example.validatingforminput.model.Registration;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    private final Map<Long, Registration> store = new ConcurrentHashMap<>();

    public Registration save(Registration registration) {
        store.put(registration.getId(), registration);
        return registration;
    }

    public Optional<Registration> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Registration> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Registration::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Registration> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String lower = query.toLowerCase();
        return store.values().stream()
                .filter(r -> r.getName().toLowerCase().contains(lower)
                        || r.getEmail().toLowerCase().contains(lower)
                        || (r.getPhone() != null && r.getPhone().contains(lower)))
                .sorted(Comparator.comparing(Registration::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public Registration update(Long id, Registration updated) {
        Registration existing = store.get(id);
        if (existing == null) {
            throw new NoSuchElementException("Registration not found: " + id);
        }
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setAge(updated.getAge());
        existing.setPhone(updated.getPhone());
        existing.setUpdatedAt(LocalDateTime.now());
        return existing;
    }

    public boolean delete(Long id) {
        return store.remove(id) != null;
    }

    public long count() {
        return store.size();
    }

    public double averageAge() {
        return store.values().stream()
                .mapToInt(Registration::getAge)
                .average()
                .orElse(0);
    }

    public boolean emailExists(String email) {
        return store.values().stream()
                .anyMatch(r -> r.getEmail().equalsIgnoreCase(email));
    }

    public boolean emailExistsExcluding(String email, Long excludeId) {
        return store.values().stream()
                .filter(r -> !r.getId().equals(excludeId))
                .anyMatch(r -> r.getEmail().equalsIgnoreCase(email));
    }

    public List<Registration> recentRegistrations(int limit) {
        return store.values().stream()
                .sorted(Comparator.comparing(Registration::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, Long> ageDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("18-25", countByAgeRange(18, 25));
        distribution.put("26-35", countByAgeRange(26, 35));
        distribution.put("36-50", countByAgeRange(36, 50));
        distribution.put("51+", countByAgeRange(51, 200));
        return distribution;
    }

    private long countByAgeRange(int min, int max) {
        return store.values().stream()
                .filter(r -> r.getAge() >= min && r.getAge() <= max)
                .count();
    }
}
