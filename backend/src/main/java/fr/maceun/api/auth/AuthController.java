package fr.maceun.api.auth;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.maceun.api.common.RateLimiter;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;
    private final RateLimiter loginRateLimiter;

    public AuthController(AuthenticationManager authenticationManager, SecurityContextRepository contextRepository,
                          @Qualifier("loginRateLimiter") RateLimiter loginRateLimiter) {
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/login")
    Map<String, String> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request, HttpServletResponse response) {
        loginRateLimiter.check(request.getRemoteAddr());
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(body.username(), body.password()));
        } catch (RuntimeException e) {
            log.warn("Échec de connexion admin depuis {}", request.getRemoteAddr());
            throw e;
        }
        // nouvelle session à chaque connexion (évite la fixation de session)
        var old = request.getSession(false);
        if (old != null) old.invalidate();
        request.getSession(true);

        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        log.info("Connexion admin : {}", auth.getName());
        return Map.of("username", auth.getName());
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    ResponseEntity<Map<String, String>> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(Map.of("username", auth.getName()));
    }
}
