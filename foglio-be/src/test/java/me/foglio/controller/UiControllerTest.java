package me.foglio.controller;

import me.foglio.model.File;
import me.foglio.model.User;
import me.foglio.repository.FileRepository;
import me.foglio.service.FileService;
import me.foglio.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UiController Tests")
class UiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private FileService fileService;

    @MockitoBean
    private FileRepository fileRepository;

    private final User testUser = User.builder().id(1L).name("Test User").apiKey(UUID.randomUUID()).build();

    @Test
    @DisplayName("Should show index page")
    void shouldShowIndexPage() throws Exception {
        // When & Then
        mockMvc.perform(get("/ui/"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should register user and redirect to api key page")
    void shouldRegisterUserAndRedirectToApiKeyPage() throws Exception {
        // Given
        String name = "Test";
        String surname = "User";
        String fullName = name + " " + surname;
        User user = User.builder().id(1L).name(fullName).apiKey(UUID.randomUUID()).build();
        when(userService.registerUser(fullName)).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/ui/register")
                        .param("name", name)
                        .param("surname", surname))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/api-key"))
                .andExpect(flash().attribute("apiKey", user.getApiKey().toString()))
                .andExpect(flash().attribute("userName", user.getName()));
    }

    @Test
    @DisplayName("Should return to login with error when name is empty")
    void shouldReturnToLoginWithErrorWhenNameIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(post("/ui/register")
                        .param("name", "")
                        .param("surname", "Surname"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/login"))
                .andExpect(flash().attribute("error", "Nome e cognome sono obbligatori"));
    }

    @Test
    @DisplayName("Should login user and redirect to files")
    void shouldLoginUserAndRedirectToFiles() throws Exception {
        // Given
        String apiKey = testUser.getApiKey().toString();
        when(userService.findByApiKey(testUser.getApiKey())).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/ui/login")
                        .param("apiKey", apiKey))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/files"))
                .andExpect(flash().attribute("message", "Logged in as " + testUser.getName()));
    }

    @Test
    @DisplayName("Should return to index with error for invalid api key")
    void shouldReturnToIndexWithErrorForInvalidApiKey() throws Exception {
        // Given
        String apiKey = UUID.randomUUID().toString();
        when(userService.findByApiKey(any())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/ui/login")
                        .param("apiKey", apiKey))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui"))
                .andExpect(flash().attribute("error", "Invalid apiKey"));
    }

    @Test
    @DisplayName("Should show files page for authenticated user")
    @WithMockUser
    void shouldShowFilesPageForAuthenticatedUser() throws Exception {
        // Given
        List<File> files = List.of(
                File.builder().id(UUID.randomUUID()).originalName("file1.txt").contentType("text/plain").size(100L).build(),
                File.builder().id(UUID.randomUUID()).originalName("file2.txt").contentType("application/pdf").size(200L).build()
        );
        when(fileRepository.findByOwner_Id(testUser.getId())).thenReturn(files);

        // When & Then
        mockMvc.perform(get("/ui/files")
                        .with(authentication(new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList()))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should show files page with empty list for unauthenticated user")
    void shouldShowFilesPageWithEmptyListForUnauthenticatedUser() throws Exception {
        // When & Then
        mockMvc.perform(get("/ui/files"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should show upload form")
    void shouldShowUploadForm() throws Exception {
        // When & Then
        mockMvc.perform(get("/ui/upload"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should upload file and redirect to files")
    @WithMockUser
    void shouldUploadFileAndRedirectToFiles() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        File savedFile = File.builder().id(UUID.randomUUID()).originalName("test.txt").build();
        when(fileService.uploadFile(any(), eq(testUser), eq(false))).thenReturn(savedFile);

        // When & Then
        mockMvc.perform(multipart("/ui/upload")
                        .file(file)
                        .with(authentication(new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList()))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/files"))
                .andExpect(flash().attribute("message", "Uploaded: test.txt"));
    }

    @Test
    @DisplayName("Should redirect to index when uploading without authentication")
    void shouldRedirectToIndexWhenUploadingWithoutAuthentication() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        // When & Then
        mockMvc.perform(multipart("/ui/upload")
                        .file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui"))
                .andExpect(flash().attribute("error", "You must be logged in to upload"));
    }

    @Test
    @DisplayName("Should logout and redirect to index")
    void shouldLogoutAndRedirectToIndex() throws Exception {
        // When & Then
        mockMvc.perform(post("/ui/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui"))
                .andExpect(flash().attribute("message", "Logged out"));
    }

    @Test
    @DisplayName("Should show preview page")
    @WithMockUser
    void shouldShowPreviewPage() throws Exception {
        // Given
        UUID fileId = UUID.randomUUID();
        File file = File.builder().id(fileId).originalName("test.png").contentType("image/png").size(100L).build();
        when(fileService.getFileMetadata(fileId)).thenReturn(Optional.of(file));

        // When & Then
        mockMvc.perform(get("/ui/file/{id}/preview", fileId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList()))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should redirect to files with error when file not found for preview")
    void shouldRedirectToFilesWithErrorWhenFileNotFoundForPreview() throws Exception {
        // Given
        UUID fileId = UUID.randomUUID();
        when(fileService.getFileMetadata(fileId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/ui/file/{id}/preview", fileId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList()))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/files"))
                .andExpect(flash().attribute("error", "File not found"));
    }

}
