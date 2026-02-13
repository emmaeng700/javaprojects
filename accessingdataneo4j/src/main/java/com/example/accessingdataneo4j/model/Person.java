package com.example.accessingdataneo4j.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Person entity for Neo4j graph database
 * Represents a team member with relationships to other team members
 */
@Node
public class Person {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String email;
    private String role;

    /**
     * Neo4j doesn't REALLY have bi-directional relationships. 
     * It just means when querying to ignore the direction of the relationship.
     * https://dzone.com/articles/modelling-data-neo4j
     */
    @Relationship(type = "TEAMMATE", direction = Relationship.Direction.OUTGOING)
    private Set<Person> teammates;

    // Constructors
    public Person() {
        // Empty constructor required for Neo4j
        this.teammates = new HashSet<>();
    }

    public Person(String name) {
        this.name = name;
        this.teammates = new HashSet<>();
    }

    public Person(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.teammates = new HashSet<>();
    }

    // Business Methods
    
    /**
     * Add a teammate relationship
     */
    public void worksWith(Person person) {
        if (teammates == null) {
            teammates = new HashSet<>();
        }
        teammates.add(person);
    }

    /**
     * Remove a teammate relationship
     */
    public void removeTeammate(Person person) {
        if (teammates != null) {
            teammates.remove(person);
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Set<Person> getTeammates() {
        if (teammates == null) {
            teammates = new HashSet<>();
        }
        return teammates;
    }

    public void setTeammates(Set<Person> teammates) {
        this.teammates = teammates;
    }

    // Object Methods

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", teammates=" + (teammates != null ? teammates.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id != null && id.equals(person.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}