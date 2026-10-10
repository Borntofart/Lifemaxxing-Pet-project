package lifemaxxing.service;

import lifemaxxing.dto.ActiveProgram;
import lifemaxxing.dto.FormError;
import lifemaxxing.dto.RegisterForm;
import lifemaxxing.exceptions.DatabaseException;
import lifemaxxing.model.User;
import lifemaxxing.model.UserProfile;
import lifemaxxing.model.WeighIn;
import lifemaxxing.persistence.ProgramMapper;
import lifemaxxing.persistence.UserMapper;
import lifemaxxing.persistence.WeighInMapper;
import lifemaxxing.security.PasswordHasher;

import java.util.Optional;

public class UserService {

    private final UserMapper userMapper;
    private final WeighInMapper weighInMapper;
    private final ProgramMapper programMapper;

    public UserService(UserMapper userMapper, WeighInMapper weighInMapper, ProgramMapper programMapper) {
        this.userMapper = userMapper;
        this.weighInMapper = weighInMapper;
        this.programMapper = programMapper;
    }

    // Samme regler som den gamle register.js, bare flyttet over i java
    public FormError validateRegistration(RegisterForm form) throws DatabaseException {
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
        if (userMapper.getUserByUsername(username) != null) {
            return new FormError("username", "Brugernavnet er allerede taget.");
        }
        if (userMapper.emailExists(email)) {
            return new FormError("email", "Der findes allerede en bruger med den email.");
        }
        return null;
    }

    // Kald kun efter validateRegistration har givet null
    public User register(RegisterForm form) throws DatabaseException {
        User user = new User(trim(form.getUsername()), trim(form.getEmail()), PasswordHasher.hash(form.getPassword()));
        return userMapper.createUser(user);
    }

    public Optional<User> login(String username, String password) throws DatabaseException {
        if (username == null || password == null) {
            return Optional.empty();
        }
        User user = userMapper.getUserByUsername(username.trim());
        if (user == null || !PasswordHasher.matches(password, user.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    // Samler brugeren fra bruger, bruger_profil, vejning og det aktive program
    public Optional<User> findById(Integer id) throws DatabaseException {
        if (id == null) {
            return Optional.empty();
        }
        User user = userMapper.getUserById(id);
        if (user == null) {
            return Optional.empty();
        }

        for (WeighIn weighIn : weighInMapper.getWeighIns(id)) {
            user.addWeighIn(weighIn);
        }

        UserProfile profile = userMapper.getProfile(id);
        ActiveProgram program = programMapper.getActiveProgram(id);
        if (profile != null && program != null) {
            WeighIn latest = user.getLatestWeighIn();
            profile.setWeightKg(latest != null ? latest.getWeightKg() : program.goal().getStartWeightKg());
            profile.setExperienceLevel(program.experienceLevel());

            user.setProfile(profile);
            user.setGoal(program.goal());
            user.setPreference(program.preference());
            user.setMacroPlan(program.macroPlan());
            user.setActiveProgramId(program.programId());
        }
        return Optional.of(user);
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
