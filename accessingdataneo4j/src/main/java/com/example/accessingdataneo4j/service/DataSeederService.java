package com.example.accessingdataneo4j.service;

import com.example.accessingdataneo4j.model.Person;
import com.example.accessingdataneo4j.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DataSeederService {

    private static final Logger log = LoggerFactory.getLogger(DataSeederService.class);
    private final PersonRepository personRepository;

    @Autowired
    public DataSeederService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public boolean isDatabaseEmpty() {
        return personRepository.count() == 0;
    }

    public void seedDemoData() {
        log.info("Seeding demo data...");
        personRepository.deleteAll();

        // Create people across different roles
        Person alice = personRepository.save(new Person("Alice Chen", "alice@company.com", "Manager"));
        Person bob = personRepository.save(new Person("Bob Smith", "bob@company.com", "Developer"));
        Person carol = personRepository.save(new Person("Carol Davis", "carol@company.com", "Developer"));
        Person david = personRepository.save(new Person("David Kim", "david@company.com", "Designer"));
        Person emma = personRepository.save(new Person("Emma Wilson", "emma@company.com", "Developer"));
        Person frank = personRepository.save(new Person("Frank Lopez", "frank@company.com", "QA"));
        Person grace = personRepository.save(new Person("Grace Patel", "grace@company.com", "DevOps"));
        Person henry = personRepository.save(new Person("Henry Zhang", "henry@company.com", "Developer"));
        Person iris = personRepository.save(new Person("Iris Johnson", "iris@company.com", "Designer"));
        Person jack = personRepository.save(new Person("Jack Brown", "jack@company.com", "Manager"));

        // Build a rich relationship web
        // Alice (Manager) — hub node, most connected
        alice.worksWith(bob);
        alice.worksWith(carol);
        alice.worksWith(david);
        alice.worksWith(jack);

        // Developer cluster
        bob.worksWith(alice);
        bob.worksWith(carol);
        bob.worksWith(emma);
        bob.worksWith(henry);

        carol.worksWith(alice);
        carol.worksWith(bob);
        carol.worksWith(emma);

        emma.worksWith(bob);
        emma.worksWith(carol);

        henry.worksWith(bob);
        henry.worksWith(jack);

        // Designer duo
        david.worksWith(alice);
        david.worksWith(iris);
        iris.worksWith(david);

        // QA-DevOps link
        frank.worksWith(grace);
        grace.worksWith(frank);
        grace.worksWith(jack);

        // Jack (Manager) — bridge node
        jack.worksWith(alice);
        jack.worksWith(henry);
        jack.worksWith(grace);

        // Save all with relationships
        personRepository.save(alice);
        personRepository.save(bob);
        personRepository.save(carol);
        personRepository.save(david);
        personRepository.save(emma);
        personRepository.save(frank);
        personRepository.save(grace);
        personRepository.save(henry);
        personRepository.save(iris);
        personRepository.save(jack);

        log.info("Demo data seeded: 10 people with team relationships");
    }
}
