package com.example.accessingdataneo4j.service;

import com.example.accessingdataneo4j.model.Person;
import com.example.accessingdataneo4j.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service layer for Person business logic
 * Handles all business operations related to Person entities
 */
@Service
@Transactional
public class PersonService {

    private static final Logger log = LoggerFactory.getLogger(PersonService.class);
    private final PersonRepository personRepository;

    @Autowired
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    // READ Operations

    /**
     * Get all people from the database
     */
    public List<Person> getAllPeople() {
        log.debug("Fetching all people");
        return personRepository.findAll();
    }

    /**
     * Get a person by their ID
     */
    public Optional<Person> getPersonById(Long id) {
        log.debug("Fetching person with id: {}", id);
        return personRepository.findById(id);
    }

    /**
     * Get a person by their exact name
     */
    public Optional<Person> getPersonByName(String name) {
        log.debug("Fetching person with name: {}", name);
        return personRepository.findByName(name);
    }

    /**
     * Search for people by name (partial, case-insensitive)
     */
    public List<Person> searchPeople(String searchTerm) {
        log.debug("Searching people with term: {}", searchTerm);
        return personRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    /**
     * Get all people with a specific role
     */
    public List<Person> getPeopleByRole(String role) {
        log.debug("Fetching people with role: {}", role);
        return personRepository.findByRole(role);
    }

    /**
     * Get all teammates of a person
     */
    public List<Person> getTeammates(Long personId) {
        log.debug("Fetching teammates for person id: {}", personId);
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new PersonNotFoundException("Person not found with id: " + personId));
        return person.getTeammates().stream().toList();
    }

    // CREATE Operation

    /**
     * Save a new person or update an existing one
     */
    public Person savePerson(Person person) {
        log.info("Saving person: {}", person.getName());
        return personRepository.save(person);
    }

    // UPDATE Operation

    /**
     * Update an existing person's details
     */
    public Person updatePerson(Long id, Person personDetails) {
        log.info("Updating person with id: {}", id);
        
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException("Person not found with id: " + id));
        
        person.setName(personDetails.getName());
        person.setEmail(personDetails.getEmail());
        person.setRole(personDetails.getRole());
        
        return personRepository.save(person);
    }

    // DELETE Operation

    /**
     * Delete a person by ID
     */
    public void deletePerson(Long id) {
        log.info("Deleting person with id: {}", id);
        
        if (!personRepository.existsById(id)) {
            throw new PersonNotFoundException("Person not found with id: " + id);
        }
        
        personRepository.deleteById(id);
    }

    // RELATIONSHIP Operations

    /**
     * Add a bidirectional teammate relationship between two people
     */
    public void addTeammate(Long personId, Long teammateId) {
        log.info("Adding teammate relationship: {} <-> {}", personId, teammateId);
        
        if (personId.equals(teammateId)) {
            throw new IllegalArgumentException("A person cannot be their own teammate");
        }
        
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new PersonNotFoundException("Person not found with id: " + personId));
        Person teammate = personRepository.findById(teammateId)
                .orElseThrow(() -> new PersonNotFoundException("Teammate not found with id: " + teammateId));
        
        // Add bidirectional relationship
        person.worksWith(teammate);
        teammate.worksWith(person);
        
        personRepository.save(person);
        personRepository.save(teammate);
    }

    /**
     * Remove a teammate relationship between two people
     */
    public void removeTeammate(Long personId, Long teammateId) {
        log.info("Removing teammate relationship: {} <-> {}", personId, teammateId);
        
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new PersonNotFoundException("Person not found with id: " + personId));
        Person teammate = personRepository.findById(teammateId)
                .orElseThrow(() -> new PersonNotFoundException("Teammate not found with id: " + teammateId));
        
        // Remove bidirectional relationship
        person.removeTeammate(teammate);
        teammate.removeTeammate(person);
        
        personRepository.save(person);
        personRepository.save(teammate);
    }

    // UTILITY Methods

    /**
     * Count total number of people
     */
    public long countPeople() {
        return personRepository.count();
    }

    /**
     * Check if a person exists by ID
     */
    public boolean personExists(Long id) {
        return personRepository.existsById(id);
    }

    // DASHBOARD Stats (computed in Java to avoid SDN multi-column mapping issues)

    public long countRelationships() {
        List<Person> all = personRepository.findAll();
        long total = 0;
        for (Person p : all) {
            total += p.getTeammates().size();
        }
        return total / 2;
    }

    public List<Person> getPeopleWithNoTeammates() {
        return personRepository.findPeopleWithNoTeammates();
    }

    public List<Map<String, Object>> getConnectionCounts() {
        List<Person> all = personRepository.findAll();
        return all.stream()
            .map(p -> Map.<String, Object>of(
                "name", p.getName(),
                "connections", (long) p.getTeammates().size()
            ))
            .sorted((a, b) -> Long.compare((Long) b.get("connections"), (Long) a.get("connections")))
            .toList();
    }

    public List<Map<String, Object>> getRoleDistribution() {
        List<Person> all = personRepository.findAll();
        LinkedHashMap<String, Long> roleCounts = new LinkedHashMap<>();
        for (Person p : all) {
            String role = p.getRole();
            if (role != null && !role.isEmpty()) {
                roleCounts.merge(role, 1L, Long::sum);
            }
        }
        return roleCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(e -> Map.<String, Object>of("role", e.getKey(), "count", e.getValue()))
            .toList();
    }

    // GRAPH Queries

    public List<Person> getShortestPath(Long fromId, Long toId) {
        // BFS in Java — more reliable than SDN Cypher mapping for path queries
        List<Person> all = personRepository.findAll();
        Map<Long, Person> peopleMap = new LinkedHashMap<>();
        for (Person p : all) { peopleMap.put(p.getId(), p); }

        if (!peopleMap.containsKey(fromId) || !peopleMap.containsKey(toId)) return List.of();

        // BFS
        Map<Long, Long> parentMap = new LinkedHashMap<>();
        java.util.Queue<Long> queue = new java.util.LinkedList<>();
        Set<Long> visited = new HashSet<>();
        queue.add(fromId);
        visited.add(fromId);
        parentMap.put(fromId, null);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (current.equals(toId)) break;
            Person p = peopleMap.get(current);
            if (p == null) continue;
            for (Person t : p.getTeammates()) {
                if (!visited.contains(t.getId()) && peopleMap.containsKey(t.getId())) {
                    visited.add(t.getId());
                    parentMap.put(t.getId(), current);
                    queue.add(t.getId());
                }
            }
        }

        if (!parentMap.containsKey(toId)) return List.of();

        // Reconstruct path
        java.util.LinkedList<Person> path = new java.util.LinkedList<>();
        Long step = toId;
        while (step != null) {
            path.addFirst(peopleMap.get(step));
            step = parentMap.get(step);
        }
        return path;
    }

    public Integer getDegreesOfSeparation(Long fromId, Long toId) {
        List<Person> path = getShortestPath(fromId, toId);
        return path.isEmpty() ? null : path.size() - 1;
    }

    public List<Map<String, Object>> getSuggestedConnections(Long personId) {
        Person person = personRepository.findById(personId).orElse(null);
        if (person == null) return List.of();

        Set<Long> myTeammateIds = new HashSet<>();
        for (Person t : person.getTeammates()) {
            myTeammateIds.add(t.getId());
        }

        // Friends-of-friends: people connected to my teammates but not to me
        LinkedHashMap<Long, Map.Entry<Person, Long>> suggestions = new LinkedHashMap<>();
        for (Person teammate : person.getTeammates()) {
            // Reload to get their teammates
            Person fullTeammate = personRepository.findById(teammate.getId()).orElse(null);
            if (fullTeammate == null) continue;
            for (Person fof : fullTeammate.getTeammates()) {
                if (fof.getId().equals(personId) || myTeammateIds.contains(fof.getId())) continue;
                suggestions.merge(fof.getId(),
                    Map.entry(fof, 1L),
                    (old, v) -> Map.entry(old.getKey(), old.getValue() + 1));
            }
        }

        return suggestions.values().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(5)
            .map(e -> Map.<String, Object>of(
                "id", e.getKey().getId(),
                "name", e.getKey().getName(),
                "mutualFriends", e.getValue()
            ))
            .toList();
    }

    /**
     * Custom exception for person not found scenarios
     */
    public static class PersonNotFoundException extends RuntimeException {
        public PersonNotFoundException(String message) {
            super(message);
        }
    }
}