package lifemaxxing.service;

import lifemaxxing.dto.FormError;
import lifemaxxing.dto.RegisterForm;
import lifemaxxing.model.User;
import lifemaxxing.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Samme regler som den gamle register.js, bare flyttet over i java
    public FormError validateRegistration(RegisterForm form) {
        String username = trim(form.getUsername());
        String email = trim(form.getEmail());
        String password = form.getPassword() == null ? "" : form.getPassword();
        String password2 = form.getPassword2() == null ? "" : form.getPassword2();

        if (username.length() < 3 || username.length() > 20) {
            return new FormError("username", "Brugernavnet skal være mellem 3 og 20 tegn.");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return new FormError("username", "Brugernavnet må kun indeholde bogstaver, tal og _");
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[a-zA-Z]{2,}$")) {
            return new FormError("email", "Indtast en gyldig email-adresse.");
        }
        if (password.length() < 6) {
            return new FormError("password", "Kodeordet skal være mindst 6 tegn.");
        }
        if (!password.matches(".*[0-9].*")) {
            return new FormError("password", "Kodeordet skal indeholde mindst ét tal.");
        }
        if (!password.equals(password2)) {
            return new FormError("password2", "De to kodeord er ikke ens.");
        }
        if (userRepository.existsByUsername(username)) {
            return new FormError("username", "Brugernavnet er allerede taget.");
        }
        return null;
    }

    // Kald kun efter validateRegistration har givet null
    public User register(RegisterForm form) {
        User user = new User(trim(form.getUsername()), trim(form.getEmail()), form.getPassword());
        return userRepository.save(user);
    }

    // TODO: kodeord skal hashes når databasen kommer på
    public Optional<User> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username.trim())
                .filter(u -> u.getPassword().equals(password));
    }

    public Optional<User> findById(UUID id) {
        return id == null ? Optional.empty() : userRepository.findById(id);
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
