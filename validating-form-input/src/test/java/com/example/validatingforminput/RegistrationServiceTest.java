package com.example.validatingforminput;

import com.example.validatingforminput.model.Registration;
import com.example.validatingforminput.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RegistrationServiceTest {

    @Autowired
    private RegistrationService service;

    @BeforeEach
    void setUp() {
        // Clear all data before each test via the service's repository
        // We'll use findAll + delete to clean up
        service.findAll().forEach(r -> service.delete(r.getId()));
    }

    @Test
    void saveShouldStoreRegistration() {
        Registration reg = new Registration("Alice", "alice@test.com", 25, null);
        Registration saved = service.save(reg);
        assertNotNull(saved.getId());
        assertEquals(1, service.count());
    }

    @Test
    void findByIdShouldReturnSavedRegistration() {
        Registration reg = new Registration("Bob", "bob@test.com", 30, "+1-555-0001");
        service.save(reg);
        Optional<Registration> found = service.findById(reg.getId());
        assertTrue(found.isPresent());
        assertEquals("Bob", found.get().getName());
    }

    @Test
    void findByIdShouldReturnEmptyForMissingId() {
        Optional<Registration> found = service.findById(999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void findAllShouldReturnInReverseChronologicalOrder() throws InterruptedException {
        service.save(new Registration("First", "first@test.com", 20, null));
        Thread.sleep(10); // ensure different timestamps
        service.save(new Registration("Second", "second@test.com", 25, null));
        List<Registration> all = service.findAll();
        assertEquals(2, all.size());
        assertEquals("Second", all.get(0).getName());
    }

    @Test
    void searchShouldFilterByName() {
        service.save(new Registration("Alice", "alice@test.com", 25, null));
        service.save(new Registration("Bob", "bob@test.com", 30, null));
        List<Registration> results = service.search("alice");
        assertEquals(1, results.size());
        assertEquals("Alice", results.get(0).getName());
    }

    @Test
    void searchShouldFilterByEmail() {
        service.save(new Registration("Alice", "alice@test.com", 25, null));
        List<Registration> results = service.search("alice@test");
        assertEquals(1, results.size());
    }

    @Test
    void searchWithBlankQueryShouldReturnAll() {
        service.save(new Registration("Alice", "alice1@test.com", 25, null));
        service.save(new Registration("Bob", "bob1@test.com", 30, null));
        List<Registration> results = service.search("");
        assertEquals(2, results.size());
    }

    @Test
    void updateShouldModifyExistingRegistration() {
        Registration reg = new Registration("Old Name", "old@test.com", 25, null);
        service.save(reg);

        Registration updated = new Registration();
        updated.setName("New Name");
        updated.setEmail("new@test.com");
        updated.setAge(30);
        updated.setPhone("+1-555-9999");

        Registration result = service.update(reg.getId(), updated);
        assertEquals("New Name", result.getName());
        assertEquals("new@test.com", result.getEmail());
        assertEquals(30, result.getAge());
    }

    @Test
    void updateShouldThrowForMissingId() {
        Registration updated = new Registration();
        updated.setName("Test");
        assertThrows(NoSuchElementException.class, () -> service.update(999L, updated));
    }

    @Test
    void deleteShouldRemoveRegistration() {
        Registration reg = new Registration("Delete Me", "delete@test.com", 25, null);
        service.save(reg);
        assertTrue(service.delete(reg.getId()));
        assertEquals(0, service.count());
    }

    @Test
    void deleteShouldReturnFalseForMissingId() {
        assertFalse(service.delete(999L));
    }

    @Test
    void emailExistsShouldBeCaseInsensitive() {
        service.save(new Registration("Alice", "Alice@Test.com", 25, null));
        assertTrue(service.emailExists("alice@test.com"));
        assertTrue(service.emailExists("ALICE@TEST.COM"));
    }

    @Test
    void emailExistsExcludingShouldExcludeGivenId() {
        Registration reg = new Registration("Alice", "alice2@test.com", 25, null);
        service.save(reg);
        assertFalse(service.emailExistsExcluding("alice2@test.com", reg.getId()));
    }

    @Test
    void averageAgeShouldCalculateCorrectly() {
        service.save(new Registration("A", "a@test.com", 20, null));
        service.save(new Registration("B", "b@test.com", 30, null));
        assertEquals(25.0, service.averageAge(), 0.01);
    }

    @Test
    void averageAgeWithNoDataShouldReturnZero() {
        assertEquals(0.0, service.averageAge());
    }

    @Test
    void ageDistributionShouldCategorizeCorrectly() {
        service.save(new Registration("Young", "y@test.com", 20, null));
        service.save(new Registration("Mid", "m@test.com", 30, null));
        service.save(new Registration("Senior", "s@test.com", 55, null));

        Map<String, Long> dist = service.ageDistribution();
        assertEquals(1L, dist.get("18-25"));
        assertEquals(1L, dist.get("26-35"));
        assertEquals(0L, dist.get("36-50"));
        assertEquals(1L, dist.get("51+"));
    }

    @Test
    void recentRegistrationsShouldLimitResults() {
        for (int i = 0; i < 10; i++) {
            service.save(new Registration("User" + i, "u" + i + "@test.com", 20 + i, null));
        }
        List<Registration> recent = service.recentRegistrations(3);
        assertEquals(5, recent.size()); // findTop5 returns max 5
    }
}
