package me.foglio.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.foglio.model.User;
import me.foglio.repository.UserRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ObjectProvider<UserRepository> userRepositoryProvider;

    public ApiKeyAuthFilter(ObjectProvider<UserRepository> userRepositoryProvider) {
        this.userRepositoryProvider = userRepositoryProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-KEY");

        // if not in header, check request parameter
        if (!StringUtils.hasText(apiKey)) {
            apiKey = request.getParameter("apiKey");
        }

        // if not in param, check cookie
        if (!StringUtils.hasText(apiKey) && request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("API_KEY".equals(c.getName()) && StringUtils.hasText(c.getValue())) {
                    apiKey = c.getValue();
                    break;
                }
            }
        }

        if (StringUtils.hasText(apiKey)) {
            try {
                UUID key = UUID.fromString(apiKey.trim());
                UserRepository userRepository = userRepositoryProvider.getIfAvailable();
                if (userRepository != null) {
                    Optional<User> u = userRepository.findByApiKey(key);
                    if (u.isPresent()) {
                        User user = u.get();
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, java.util.Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        request.setAttribute("authenticatedUser", user);
                    }
                }
            } catch (IllegalArgumentException ignored) {
                // invalid UUID — ignore and continue without auth
            }
        }

        filterChain.doFilter(request, response);
    }
}
