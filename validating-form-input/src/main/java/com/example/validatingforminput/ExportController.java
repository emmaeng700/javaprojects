package com.example.validatingforminput;

import com.example.validatingforminput.model.Registration;
import com.example.validatingforminput.service.RegistrationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class ExportController {

    private final RegistrationService registrationService;

    public ExportController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/submissions/export")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=registrations.csv");

        List<Registration> registrations = registrationService.findAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("ID,Name,Email,Age,Phone,Registered At,Updated At");
            for (Registration reg : registrations) {
                writer.printf("%d,\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\"%n",
                        reg.getId(),
                        escapeCsv(reg.getName()),
                        escapeCsv(reg.getEmail()),
                        reg.getAge(),
                        escapeCsv(reg.getPhone() != null ? reg.getPhone() : ""),
                        reg.getCreatedAt().format(fmt),
                        reg.getUpdatedAt().format(fmt));
            }
        }
    }

    private String escapeCsv(String value) {
        return value.replace("\"", "\"\"");
    }
}
