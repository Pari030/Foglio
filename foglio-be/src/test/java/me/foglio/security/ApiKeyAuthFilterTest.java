package me.foglio.security;

import jakarta.servlet.FilterChain;
import me.foglio.model.User;
import me.foglio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiKeyAuthFilter Tests")
class ApiKeyAuthFilterTest {

    @Mock(lenient = true)
    private ObjectProvider<UserRepository> userRepositoryProvider;

    @Mock(lenient = true)
    private UserRepository userRepository;

    @Mock(lenient = true)
    private FilterChain filterChain;

    @InjectMocks
    private ApiKeyAuthFilter apiKeyAuthFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate user with valid api key from header")
    void shouldAuthenticateUserWithValidApiKeyFromHeader() throws Exception {
        // Given
        UUID apiKey = UUID.randomUUID();
        User user = User.builder().id(1L).name("Test User").apiKey(apiKey).build();
        request.addHeader("X-API-KEY", apiKey.toString());
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.findByApiKey(apiKey)).thenReturn(Optional.of(user));

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
    }

    @Test
    @DisplayName("Should authenticate user with valid api key from parameter")
    void shouldAuthenticateUserWithValidApiKeyFromParameter() throws Exception {
        // Given
        UUID apiKey = UUID.randomUUID();
        User user = User.builder().id(1L).name("Test User").apiKey(apiKey).build();
        request.setParameter("apiKey", apiKey.toString());
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.findByApiKey(apiKey)).thenReturn(Optional.of(user));

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("Should authenticate user with valid api key from cookie")
    void shouldAuthenticateUserWithValidApiKeyFromCookie() throws Exception {
        // Given
        UUID apiKey = UUID.randomUUID();
        User user = User.builder().id(1L).name("Test User").apiKey(apiKey).build();
        request.setCookies(new jakarta.servlet.http.Cookie("API_KEY", apiKey.toString()));
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.findByApiKey(apiKey)).thenReturn(Optional.of(user));

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("Should not authenticate with invalid api key")
    void shouldNotAuthenticateWithInvalidApiKey() throws Exception {
        // Given
        request.addHeader("X-API-KEY", "invalid-uuid");
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should not authenticate when api key not found")
    void shouldNotAuthenticateWhenApiKeyNotFound() throws Exception {
        // Given
        UUID apiKey = UUID.randomUUID();
        request.addHeader("X-API-KEY", apiKey.toString());
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.findByApiKey(apiKey)).thenReturn(Optional.empty());

        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should not authenticate when no api key provided")
    void shouldNotAuthenticateWhenNoApiKeyProvided() throws Exception {
        // When
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userRepository, never()).findByApiKey(any());
    }
}
