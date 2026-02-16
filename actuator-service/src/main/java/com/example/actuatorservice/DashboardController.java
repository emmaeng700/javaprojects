package com.example.actuatorservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Value("${server.port}")
    private int serverPort;

    @Value("${management.server.port}")
    private int managementPort;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("serverPort", serverPort);
        model.addAttribute("managementPort", managementPort);
        return "dashboard";
    }

    @GetMapping("/explorer")
    public String explorer(Model model) {
        model.addAttribute("serverPort", serverPort);
        model.addAttribute("managementPort", managementPort);
        return "explorer";
    }

    @GetMapping("/endpoints")
    public String endpoints(Model model) {
        model.addAttribute("serverPort", serverPort);
        model.addAttribute("managementPort", managementPort);
        return "endpoints";
    }

    @GetMapping("/monitor")
    public String monitor(Model model) {
        model.addAttribute("serverPort", serverPort);
        model.addAttribute("managementPort", managementPort);
        return "monitor";
    }
}
