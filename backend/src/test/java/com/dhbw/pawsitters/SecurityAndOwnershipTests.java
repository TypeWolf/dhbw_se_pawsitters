package com.dhbw.pawsitters;

import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.repository.user.AppUserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndOwnershipTests {

    private static final String PASSWORD = "SecurePass123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerHashesPasswordAndDoesNotSerializeIt() throws Exception {
        String email = uniqueEmail("owner");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "firstName", "Ada",
                                "lastName", "Lovelace",
                                "email", email,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();

        JsonNode response = json(result);
        assertThat(response.has("role")).isTrue();

        AppUser saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, saved.getPasswordHash())).isTrue();
    }

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/requests/available"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestLifecycleEnforcesServerSideOwnership() throws Exception {
        SessionContext owner = registerAndLogin("owner");
        SessionContext sitter = registerAndLogin("sitter");

        Long petId = createPet(owner, "Milo");

        mockMvc.perform(post("/api/requests")
                        .session(sitter.session())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(requestBody(petId))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PET_NOT_FOUND"));

        Long requestId = createRequest(owner, petId);

        mockMvc.perform(put("/api/requests/{id}/accept", requestId)
                        .session(owner.session())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CANNOT_ACCEPT_OWN_REQUEST"));

        mockMvc.perform(put("/api/requests/{id}/accept", requestId)
                        .session(sitter.session())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(delete("/api/requests/{id}", requestId)
                        .session(sitter.session())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("REQUEST_DELETE_FORBIDDEN"));

        mockMvc.perform(delete("/api/requests/{id}", requestId)
                        .session(owner.session())
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpointsRequireAdminRole() throws Exception {
        SessionContext normalUser = registerAndLogin("plain");

        mockMvc.perform(get("/api/admin/users")
                        .session(normalUser.session()))
                .andExpect(status().isForbidden());

        String adminEmail = uniqueEmail("admin");
        userRepository.save(AppUser.builder()
                .firstName("Admin")
                .lastName("User")
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .role(AppUser.Role.ADMIN)
                .build());

        SessionContext admin = login(adminEmail);

        mockMvc.perform(get("/api/admin/users")
                        .session(admin.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    private SessionContext registerAndLogin(String prefix) throws Exception {
        String email = uniqueEmail(prefix);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "firstName", prefix,
                                "lastName", "Tester",
                                "email", email,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk());
        return login(email);
    }

    private SessionContext login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return new SessionContext(session);
    }

    private Long createPet(SessionContext owner, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/me/pets")
                        .session(owner.session())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", name,
                                "species", "Dog",
                                "breed", "Mixed",
                                "age", 4
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner.firstName").exists())
                .andReturn();
        return json(result).get("id").asLong();
    }

    private Long createRequest(SessionContext owner, Long petId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/requests")
                        .session(owner.session())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(requestBody(petId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        return json(result).get("id").asLong();
    }

    private Map<String, Object> requestBody(Long petId) {
        return Map.of(
                "petId", petId,
                "startTime", LocalDateTime.now().plusDays(2).withNano(0).toString(),
                "endTime", LocalDateTime.now().plusDays(3).withNano(0).toString()
        );
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.com";
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private record SessionContext(MockHttpSession session) {
    }
}
