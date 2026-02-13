package com.example.accessingdataneo4j.controller;

import com.example.accessingdataneo4j.service.DataSeederService;
import com.example.accessingdataneo4j.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    private final PersonService personService;
    private final DataSeederService dataSeederService;

    @Autowired
    public DashboardController(PersonService personService, DataSeederService dataSeederService) {
        this.personService = personService;
        this.dataSeederService = dataSeederService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        log.info("Loading dashboard");

        try {
            long totalPeople = personService.countPeople();
            long totalConnections = personService.countRelationships();
            List<Map<String, Object>> connectionRanking = personService.getConnectionCounts();
            List<Map<String, Object>> roleDistribution = personService.getRoleDistribution();

            model.addAttribute("totalPeople", totalPeople);
            model.addAttribute("totalConnections", totalConnections);
            model.addAttribute("connectionRanking", connectionRanking);
            model.addAttribute("roleDistribution", roleDistribution);
            model.addAttribute("isEmpty", totalPeople == 0);

            // Find most connected person from ranking
            if (!connectionRanking.isEmpty()) {
                model.addAttribute("topPerson", connectionRanking.get(0).get("name"));
                model.addAttribute("topConnections", connectionRanking.get(0).get("connections"));
            }

            // Lonely people count
            model.addAttribute("lonelyCount", personService.getPeopleWithNoTeammates().size());

        } catch (Exception e) {
            log.error("Error loading dashboard", e);
            model.addAttribute("error", "Could not load dashboard data. Is Neo4j running?");
            model.addAttribute("isEmpty", true);
            model.addAttribute("totalPeople", 0);
            model.addAttribute("totalConnections", 0);
            model.addAttribute("lonelyCount", 0);
        }

        return "dashboard";
    }

    @PostMapping("/seed")
    public String seedData(RedirectAttributes redirectAttributes) {
        log.info("Seeding demo data");
        try {
            dataSeederService.seedDemoData();
            redirectAttributes.addFlashAttribute("success",
                "Demo data loaded! 10 people with team relationships created.");
        } catch (Exception e) {
            log.error("Error seeding data", e);
            redirectAttributes.addFlashAttribute("error",
                "Error seeding data: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }
}
