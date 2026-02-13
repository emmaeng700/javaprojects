package com.example.accessingdataneo4j.controller;

import com.example.accessingdataneo4j.model.Person;
import com.example.accessingdataneo4j.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class GraphApiController {

    private static final Logger log = LoggerFactory.getLogger(GraphApiController.class);
    private final PersonService personService;

    @Autowired
    public GraphApiController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/graph")
    public String graphPage() {
        return "graph";
    }

    @GetMapping("/api/graph")
    @ResponseBody
    public Map<String, Object> getGraphData() {
        log.info("Fetching graph data");
        List<Person> allPeople = personService.getAllPeople();

        List<Map<String, Object>> nodes = new ArrayList<>();
        Set<String> edgeKeys = new HashSet<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        for (Person p : allPeople) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", p.getId());
            node.put("label", p.getName());
            node.put("role", p.getRole() != null ? p.getRole() : "Unknown");
            node.put("email", p.getEmail() != null ? p.getEmail() : "");
            node.put("teammateCount", p.getTeammates().size());
            nodes.add(node);

            for (Person t : p.getTeammates()) {
                long minId = Math.min(p.getId(), t.getId());
                long maxId = Math.max(p.getId(), t.getId());
                String key = minId + "-" + maxId;
                if (edgeKeys.add(key)) {
                    Map<String, Object> edge = new HashMap<>();
                    edge.put("from", p.getId());
                    edge.put("to", t.getId());
                    edges.add(edge);
                }
            }
        }

        return Map.of("nodes", nodes, "edges", edges);
    }

    @GetMapping("/api/shortest-path")
    @ResponseBody
    public Map<String, Object> shortestPath(@RequestParam Long from, @RequestParam Long to) {
        log.info("Finding shortest path: {} -> {}", from, to);
        try {
            List<Person> path = personService.getShortestPath(from, to);
            Integer degrees = personService.getDegreesOfSeparation(from, to);

            List<Map<String, Object>> pathNodes = new ArrayList<>();
            for (Person p : path) {
                pathNodes.add(Map.of("id", p.getId(), "name", p.getName()));
            }

            return Map.of(
                "path", pathNodes,
                "degrees", degrees != null ? degrees : -1,
                "found", !path.isEmpty()
            );
        } catch (Exception e) {
            log.error("Error finding shortest path", e);
            return Map.of("path", List.of(), "degrees", -1, "found", false);
        }
    }

    @GetMapping("/api/suggestions/{personId}")
    @ResponseBody
    public List<Map<String, Object>> suggestions(@PathVariable Long personId) {
        log.info("Getting suggestions for person: {}", personId);
        try {
            return personService.getSuggestedConnections(personId);
        } catch (Exception e) {
            log.error("Error getting suggestions", e);
            return List.of();
        }
    }
}
