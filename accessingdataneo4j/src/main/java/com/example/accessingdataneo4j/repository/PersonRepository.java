package com.example.accessingdataneo4j.repository;

import com.example.accessingdataneo4j.model.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Person entities
 * Spring Data Neo4j will automatically implement this interface
 */
@Repository
public interface PersonRepository extends Neo4jRepository<Person, Long> {

    /**
     * Find a person by exact name match
     */
    Optional<Person> findByName(String name);

    /**
     * Search for people by name (case-insensitive, partial match)
     */
    List<Person> findByNameContainingIgnoreCase(String name);

    /**
     * Find all people with a specific role
     */
    List<Person> findByRole(String role);

    /**
     * Custom query: Find all teammates of a person by their name
     */
    @Query("MATCH (p:Person)-[:TEAMMATE]->(teammate:Person) WHERE p.name = $name RETURN teammate")
    List<Person> findTeammatesByPersonName(String name);

    /**
     * Custom query: Add bidirectional teammate relationship
     */
    @Query("MATCH (p:Person) WHERE id(p) = $personId " +
           "MATCH (t:Person) WHERE id(t) = $teammateId " +
           "MERGE (p)-[:TEAMMATE]->(t) " +
           "MERGE (t)-[:TEAMMATE]->(p)")
    void addTeammateRelationship(Long personId, Long teammateId);

    /**
     * Custom query: Remove bidirectional teammate relationship
     */
    @Query("MATCH (p:Person)-[r:TEAMMATE]-(t:Person) " +
           "WHERE id(p) = $personId AND id(t) = $teammateId " +
           "DELETE r")
    void removeTeammateRelationship(Long personId, Long teammateId);

    /**
     * Custom query: Count total number of people
     */
    @Query("MATCH (p:Person) RETURN count(p)")
    Long countAllPeople();

    /**
     * Custom query: Find people with no teammates
     */
    @Query("MATCH (p:Person) WHERE NOT (p)-[:TEAMMATE]-() RETURN p")
    List<Person> findPeopleWithNoTeammates();

}