package org.springframework.samples.petclinic.auth;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

	private final UserAccountRepository userAccountRepository;

	private final PasswordService passwordService;

	public AuthController(UserAccountRepository userAccountRepository, PasswordService passwordService) {
		this.userAccountRepository = userAccountRepository;
		this.passwordService = passwordService;
	}

	@GetMapping("/register")
	public String registerForm(Model model, HttpSession session) {
		if (session.getAttribute("loggedInUser") != null) {
			return "redirect:/";
		}
		model.addAttribute("registerForm", new RegisterForm());
		return "auth/register";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute RegisterForm registerForm, BindingResult result, HttpSession session) {
		String username = registerForm.getUsername() == null ? "" : registerForm.getUsername().trim();
		String email = registerForm.getEmail() == null ? "" : registerForm.getEmail().trim();

		if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
			result.rejectValue("username", "duplicate", "Username is already in use");
		}
		if (userAccountRepository.existsByEmailIgnoreCase(email)) {
			result.rejectValue("email", "duplicate", "Email is already in use");
		}
		if (!Objects.equals(registerForm.getPassword(), registerForm.getConfirmPassword())) {
			result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
		}
		if (result.hasErrors()) {
			return "auth/register";
		}

		UserAccount user = new UserAccount();
		user.setUsername(username);
		user.setEmail(email);
		user.setPasswordHash(passwordService.hash(registerForm.getPassword()));
		userAccountRepository.save(user);
		return "redirect:/login?registered";
	}

	@GetMapping("/login")
	public String loginForm(Model model, HttpSession session) {
		if (session.getAttribute("loggedInUser") != null) {
			return "redirect:/";
		}
		model.addAttribute("loginForm", new LoginForm());
		return "auth/login";
	}

	@PostMapping("/login")
	public String login(@Valid @ModelAttribute LoginForm loginForm, BindingResult result, HttpSession session) {
		if (result.hasErrors()) {
			return "auth/login";
		}

		UserAccount user = userAccountRepository.findByUsernameIgnoreCase(loginForm.getUsername().trim()).orElse(null);
		if (user == null || !passwordService.matches(loginForm.getPassword(), user.getPasswordHash())) {
			result.reject("invalid", "Incorrect username or password");
			return "auth/login";
		}

		session.setAttribute("loggedInUser", user.getUsername());
		return "redirect:/";
	}

	@PostMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login";
	}

}
