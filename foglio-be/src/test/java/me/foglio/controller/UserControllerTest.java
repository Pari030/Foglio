package me.foglio.controller;

import me.foglio.model.User;
import me.foglio.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        String name = "Test User";
        UUID apiKey = UUID.randomUUID();
        User user = User.builder().id(1L).name(name).apiKey(apiKey).build();
        when(userService.registerUser(name)).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .param("name", name)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.apiKey").value(apiKey.toString()));
    }

    @Test
    @DisplayName("Should return 400 when name is empty")
    void shouldReturn400WhenNameIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .param("name", "")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get current user info")
    @WithMockUser
    void shouldGetCurrentUserInfo() throws Exception {
        // Given
        User user = User.builder().id(1L).name("Test User").apiKey(UUID.randomUUID()).build();

        // When & Then
        mockMvc.perform(get("/api/users/me")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.apiKey").value(user.getApiKey().toString()));
    }

    @Test
    @DisplayName("Should return 401 when accessing me without authentication")
    void shouldReturn401WhenAccessingMeWithoutAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
