package pt.ul.fc.ptir.webtaxi.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/session")
public class SessionController {

    @GetMapping("/create")
    public String createSession(HttpSession session) {
        session.setAttribute("username", "JohnDoe");
        String sessionId = session.getId();
        return "Session created with ID: " + sessionId;
    }

    @GetMapping("/get")
    public String getSession(HttpSession session) {
        // Get the session attribute (username)
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return "No session found!";
        }

        return "Session found with username: " + username;
    }

    @GetMapping("/invalidate")
    public String invalidateSession(HttpSession session) {
        session.invalidate();
        return "Session invalidated!";
    }
}