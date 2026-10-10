package lifemaxxing.controller;

import jakarta.servlet.http.HttpSession;

// Samlet sted for hvad der ligger i sessionen
public final class SessionUtil {

    private static final String USER_ID = "userId";

    private SessionUtil() {}

    public static void login(HttpSession session, int userId) {
        session.setAttribute(USER_ID, userId);
    }

    public static Integer getUserId(HttpSession session) {
        return (Integer) session.getAttribute(USER_ID);
    }

    public static boolean isLoggedIn(HttpSession session) {
        return getUserId(session) != null;
    }
}
