package com.example.accessingdataneo4j.controller;

import com.example.accessingdataneo4j.model.Person;
import com.example.accessingdataneo4j.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Web Controller for Person management
 * Handles all HTTP requests and returns Thymeleaf views
 */
@Controller
@RequestMapping("/")
public class PersonController {

    private static final Logger log = LoggerFactory.getLogger(PersonController.class);
    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Redirect root to people list
     */
    @GetMapping
    public String index() {
        return "redirect:/dashboard";
    }

    /**
     * List all people with optional search
     * GET /people or GET /people?search=name
     */
    @GetMapping("/people")
    public String listPeople(
            @RequestParam(required = false) String search,
            Model model) {

        log.info("Listing people, search term: {}", search);

        List<Person> people;
        try {
            if (search != null && !search.trim().isEmpty()) {
                people = personService.searchPeople(search);
                model.addAttribute("search", search);
            } else {
                people = personService.getAllPeople();
            }
        } catch (Exception e) {
            log.error("Failed to fetch people (is Neo4j running?)", e);
            people = List.of();
            model.addAttribute("error",
                "Could not connect to database. Make sure Neo4j is running on bolt://localhost:7687");
        }

        model.addAttribute("people", people);
        model.addAttribute("totalCount", people.size());

        return "people/list";
    }

    /**
     * Show form to create a new person
     * GET /people/new
     */
    @GetMapping("/people/new")
    public String showCreateForm(Model model) {
        log.info("Showing create person form");
        model.addAttribute("person", new Person());
        model.addAttribute("isEdit", false);
        return "people/form";
    }

    /**
     * Create a new person
     * POST /people
     */
    @PostMapping("/people")
    public String createPerson(
            @ModelAttribute Person person, 
            RedirectAttributes redirectAttributes) {
        
        log.info("Creating new person: {}", person.getName());
        
        try {
            personService.savePerson(person);
            redirectAttributes.addFlashAttribute("success", 
                "Person '" + person.getName() + "' created successfully!");
            return "redirect:/people";
        } catch (Exception e) {
            log.error("Error creating person", e);
            redirectAttributes.addFlashAttribute("error", 
                "Error creating person: " + e.getMessage());
            return "redirect:/people/new";
        }
    }

    /**
     * View a single person's details
     * GET /people/{id}
     */
    @GetMapping("/people/{id}")
    public String viewPerson(
            @PathVariable Long id, 
            Model model,
            RedirectAttributes redirectAttributes) {
        
        log.info("Viewing person with id: {}", id);
        
        try {
            Person person = personService.getPersonById(id)
                    .orElseThrow(() -> new PersonService.PersonNotFoundException(
                        "Person not found with id: " + id));
            
            // Get all people except current person and their existing teammates
            List<Person> allPeople = personService.getAllPeople();
            allPeople.removeIf(p -> 
                p.getId().equals(id) || 
                (person.getTeammates() != null && person.getTeammates().contains(p))
            );
            
            model.addAttribute("person", person);
            model.addAttribute("availableTeammates", allPeople);
            
            return "people/view";
            
        } catch (PersonService.PersonNotFoundException e) {
            log.error("Person not found: {}", id);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/people";
        }
    }

    /**
     * Show form to edit an existing person
     * GET /people/{id}/edit
     */
    @GetMapping("/people/{id}/edit")
    public String showEditForm(
            @PathVariable Long id, 
            Model model,
            RedirectAttributes redirectAttributes) {
        
        log.info("Showing edit form for person id: {}", id);
        
        try {
            Person person = personService.getPersonById(id)
                    .orElseThrow(() -> new PersonService.PersonNotFoundException(
                        "Person not found with id: " + id));
            
            model.addAttribute("person", person);
            model.addAttribute("isEdit", true);
            
            return "people/form";
            
        } catch (PersonService.PersonNotFoundException e) {
            log.error("Person not found: {}", id);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/people";
        }
    }

    /**
     * Update an existing person
     * POST /people/{id}
     */
    @PostMapping("/people/{id}")
    public String updatePerson(
            @PathVariable Long id, 
            @ModelAttribute Person person, 
            RedirectAttributes redirectAttributes) {
        
        log.info("Updating person with id: {}", id);
        
        try {
            personService.updatePerson(id, person);
            redirectAttributes.addFlashAttribute("success", 
                "Person '" + person.getName() + "' updated successfully!");
            return "redirect:/people/" + id;
            
        } catch (PersonService.PersonNotFoundException e) {
            log.error("Person not found: {}", id);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/people";
        } catch (Exception e) {
            log.error("Error updating person", e);
            redirectAttributes.addFlashAttribute("error", 
                "Error updating person: " + e.getMessage());
            return "redirect:/people/" + id + "/edit";
        }
    }

    /**
     * Delete a person
     * POST /people/{id}/delete
     */
    @PostMapping("/people/{id}/delete")
    public String deletePerson(
            @PathVariable Long id, 
            RedirectAttributes redirectAttributes) {
        
        log.info("Deleting person with id: {}", id);
        
        try {
            Person person = personService.getPersonById(id).orElse(null);
            String name = person != null ? person.getName() : "Person";
            
            personService.deletePerson(id);
            redirectAttributes.addFlashAttribute("success", 
                name + " deleted successfully!");
            return "redirect:/people";
            
        } catch (PersonService.PersonNotFoundException e) {
            log.error("Person not found: {}", id);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/people";
        } catch (Exception e) {
            log.error("Error deleting person", e);
            redirectAttributes.addFlashAttribute("error", 
                "Error deleting person: " + e.getMessage());
            return "redirect:/people";
        }
    }

    /**
     * Add a teammate relationship
     * POST /people/{personId}/teammates/{teammateId}
     */
    @PostMapping("/people/{personId}/teammates/{teammateId}")
    public String addTeammate(
            @PathVariable Long personId, 
            @PathVariable Long teammateId,
            RedirectAttributes redirectAttributes) {
        
        log.info("Adding teammate relationship: {} <-> {}", personId, teammateId);
        
        try {
            personService.addTeammate(personId, teammateId);
            redirectAttributes.addFlashAttribute("success", 
                "Teammate added successfully!");
            return "redirect:/people/" + personId;
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid teammate operation", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/people/" + personId;
        } catch (PersonService.PersonNotFoundException e) {
            log.error("Person not found", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/people";
        } catch (Exception e) {
            log.error("Error adding teammate", e);
            redirectAttributes.addFlashAttribute("error", 
                "Error adding teammate: " + e.getMessage());
            return "redirect:/people/" + personId;
        }
    }

    /**
     * Remove a teammate relationship
     * POST /people/{personId}/teammates/{teammateId}/remove
     */
    @PostMapping("/people/{personId}/teammates/{teammateId}/remove")
    public String removeTeammate(
            @PathVariable Long personId, 
            @PathVariable Long teammateId,
            RedirectAttributes redirectAttributes) {
        
        log.info("Removing teammate relationship: {} <-> {}", personId, teammateId);
        
        try {
            personService.removeTeammate(personId, teammateId);
            redirectAttributes.addFlashAttribute("success", 
                "Teammate removed successfully!");
            return "redirect:/people/" + personId;
            
        } catch (PersonService.PersonNotFoundException e) {
            log.error("Person not found", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/people";
        } catch (Exception e) {
            log.error("Error removing teammate", e);
            redirectAttributes.addFlashAttribute("error", 
                "Error removing teammate: " + e.getMessage());
            return "redirect:/people/" + personId;
        }
    }

    /**
     * Exception handler for this controller
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("Unexpected error in controller", e);
        model.addAttribute("error",
            "An unexpected error occurred: " + e.getMessage());
        model.addAttribute("people", List.of());
        model.addAttribute("totalCount", 0);
        return "people/list";
    }
}