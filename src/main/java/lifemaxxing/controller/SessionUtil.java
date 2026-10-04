package lifemaxxing.controller;

import jakarta.servlet.http.HttpSession;

import java.util.UUID;

// Samlet sted for hvad der ligger i sessionen
public final class SessionUtil {

    private static final String USER_ID = "userId";

    private SessionUtil() {}

    public static void login(HttpSession session, UUID userId) {
        session.setAttribute(USER_ID, userId);
    }

    public static UUID getUserId(HttpSession session) {
        return (UUID) session.getAttribute(USER_ID);
    }

    public static boolean isLoggedIn(HttpSession session) {
        return getUserId(session) != null;
    }
}
