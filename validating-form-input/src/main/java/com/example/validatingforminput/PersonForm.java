package com.example.validatingforminput;

import com.example.validatingforminput.validation.PasswordMatch;
import com.example.validatingforminput.validation.ValidPhone;
import jakarta.validation.constraints.*;

@PasswordMatch
public class PersonForm {

	@NotNull
	@Size(min = 2, max = 30, message = "Name must be between {min} and {max} characters")
	private String name;

	@NotNull
	@Email(message = "Please provide a valid email address")
	@NotBlank(message = "Email is required")
	private String email;

	@NotNull(message = "Age is required")
	@Min(value = 18, message = "You must be at least {value} years old")
	@Max(value = 150, message = "Age must be realistic")
	private Integer age;

	@ValidPhone
	private String phone;

	@NotNull
	@Size(min = 8, max = 64, message = "Password must be between {min} and {max} characters")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
			message = "Password must contain at least one uppercase, one lowercase, and one digit")
	private String password;

	@NotNull(message = "Please confirm your password")
	private String confirmPassword;

	// --- Getters and Setters ---

	public String getName() {
		return this.name;
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

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	@Override
	public String toString() {
		return "Person(Name: " + name + ", Email: " + email + ", Age: " + age + ")";
	}
}
