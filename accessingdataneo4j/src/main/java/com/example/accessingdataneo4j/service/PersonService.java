package com.example.accessingdataneo4j.service;

import com.example.accessingdataneo4j.model.Person;
import com.example.accessingdataneo4j.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    /**
     * Custom exception for person not found scenarios
     */
    public static class PersonNotFoundException extends RuntimeException {
        public PersonNotFoundException(String message) {
            super(message);
        }
    }
}