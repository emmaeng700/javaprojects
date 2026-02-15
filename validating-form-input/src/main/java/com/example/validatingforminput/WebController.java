package com.example.validatingforminput;

import com.example.validatingforminput.model.Registration;
import com.example.validatingforminput.service.RegistrationService;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {

	private final RegistrationService registrationService;

	public WebController(RegistrationService registrationService) {
		this.registrationService = registrationService;
	}

	// --- Registration Form ---

	@GetMapping("/")
	public String showForm(PersonForm personForm) {
		return "form";
	}

	@PostMapping("/")
	public String checkPersonInfo(@Valid PersonForm personForm, BindingResult bindingResult,
								  RedirectAttributes redirectAttributes) {
		if (registrationService.emailExists(personForm.getEmail())) {
			bindingResult.rejectValue("email", "duplicate", "This email is already registered");
		}

		if (bindingResult.hasErrors()) {
			return "form";
		}

		Registration registration = new Registration(
				personForm.getName(),
				personForm.getEmail(),
				personForm.getAge(),
				personForm.getPhone()
		);
		registrationService.save(registration);

		redirectAttributes.addFlashAttribute("success",
				"Welcome, " + personForm.getName() + "! Registration #" + registration.getId() + " created.");
		return "redirect:/results";
	}

	// --- Results Page ---

	@GetMapping("/results")
	public String results() {
		return "results";
	}

	// --- Dashboard ---

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("totalRegistrations", registrationService.count());
		model.addAttribute("averageAge", String.format("%.1f", registrationService.averageAge()));
		model.addAttribute("recentRegistrations", registrationService.recentRegistrations(5));
		model.addAttribute("ageDistribution", registrationService.ageDistribution());
		return "dashboard";
	}

	// --- Submissions List ---

	@GetMapping("/submissions")
	public String submissions(@RequestParam(required = false) String search, Model model) {
		if (search != null && !search.isBlank()) {
			model.addAttribute("registrations", registrationService.search(search));
			model.addAttribute("searchQuery", search);
		} else {
			model.addAttribute("registrations", registrationService.findAll());
		}
		model.addAttribute("totalCount", registrationService.count());
		return "submissions";
	}

	// --- Edit ---

	@GetMapping("/submissions/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Registration reg = registrationService.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Registration not found: " + id));

		PersonForm form = new PersonForm();
		form.setName(reg.getName());
		form.setEmail(reg.getEmail());
		form.setAge(reg.getAge());
		form.setPhone(reg.getPhone());
		form.setPassword("Placeholder1");
		form.setConfirmPassword("Placeholder1");

		model.addAttribute("personForm", form);
		model.addAttribute("registrationId", id);
		model.addAttribute("isEdit", true);
		return "edit";
	}

	@PostMapping("/submissions/{id}/edit")
	public String updateRegistration(@PathVariable Long id,
									 @Valid PersonForm personForm,
									 BindingResult bindingResult,
									 Model model,
									 RedirectAttributes redirectAttributes) {
		if (registrationService.emailExistsExcluding(personForm.getEmail(), id)) {
			bindingResult.rejectValue("email", "duplicate", "This email is already registered");
		}

		if (bindingResult.hasErrors()) {
			model.addAttribute("registrationId", id);
			model.addAttribute("isEdit", true);
			return "edit";
		}

		Registration updated = new Registration();
		updated.setName(personForm.getName());
		updated.setEmail(personForm.getEmail());
		updated.setAge(personForm.getAge());
		updated.setPhone(personForm.getPhone());
		registrationService.update(id, updated);

		redirectAttributes.addFlashAttribute("success", "Registration updated successfully.");
		return "redirect:/submissions";
	}

	// --- Delete ---

	@PostMapping("/submissions/{id}/delete")
	public String deleteRegistration(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		if (registrationService.delete(id)) {
			redirectAttributes.addFlashAttribute("success", "Registration deleted.");
		} else {
			redirectAttributes.addFlashAttribute("error", "Registration not found.");
		}
		return "redirect:/submissions";
	}
}
