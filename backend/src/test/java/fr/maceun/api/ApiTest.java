package fr.maceun.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiTest {

    private static final String RELEASE_JSON = """
            {"title":"Nouveau Son","genre":"Techno","releaseDate":"2027-01-15",
             "cover":"/img/djm1.jpg","url":"https://maceun.fanlink.tv/nouveau","visible":true}
            """;

    @Autowired
    MockMvc mvc;

    // ----- public -----

    @Test
    void releasesReturnsLatestFirst() throws Exception {
        mvc.perform(get("/api/releases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previous[0].number").exists());
    }

    @Test
    void photosAreListed() throws Exception {
        mvc.perform(get("/api/photos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].src").exists());
    }

    @Test
    void bookingRejectsInvalidEmail() throws Exception {
        mvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Test","email":"pas-un-email","message":"Anniversaire le 12 juin à Lyon"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").exists());
    }

    @Test
    void bookingHoneypotIsSilentlyAccepted() throws Exception {
        mvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Bot","email":"bot@spam.io","message":"Buy cheap stuff now!!","website":"http://spam"}
                                """))
                .andExpect(status().isAccepted());
    }

    // ----- sécurité -----

    @Test
    void adminRequiresLogin() throws Exception {
        mvc.perform(get("/api/admin/releases")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"maceun\",\"password\":\"mauvais\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginGivesAccessToAdmin() throws Exception {
        var session = (MockHttpSession) mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"maceun\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);

        mvc.perform(get("/api/admin/releases").session(session)).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.username").value("maceun"));
    }

    @Test
    void adminWriteWithoutCsrfIsRefused() throws Exception {
        mvc.perform(post("/api/admin/releases").with(user("maceun").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(RELEASE_JSON))
                .andExpect(status().isForbidden());
    }

    // ----- admin -----

    @Test
    void releaseCrud() throws Exception {
        var admin = user("maceun").roles("ADMIN");
        var body = mvc.perform(post("/api/admin/releases").with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(RELEASE_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Nouveau Son"))
                .andReturn().getResponse().getContentAsString();
        var id = body.replaceAll(".*\"id\":(\\d+).*", "$1");

        // la sortie la plus récente devient la "latest" du site
        mvc.perform(get("/api/releases")).andExpect(jsonPath("$.latest.title").value("Nouveau Son"));

        mvc.perform(put("/api/admin/releases/" + id).with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(RELEASE_JSON.replace("\"visible\":true", "\"visible\":false")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/releases")).andExpect(jsonPath("$.latest.title").value("I Lose Myself"));

        mvc.perform(delete("/api/admin/releases/" + id).with(admin).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(delete("/api/admin/releases/" + id).with(admin).with(csrf())).andExpect(status().isNotFound());
    }

    @Test
    void releaseValidation() throws Exception {
        mvc.perform(post("/api/admin/releases").with(user("maceun").roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"genre\":\"Techno\",\"releaseDate\":\"2027-01-15\",\"cover\":\"/img/x.jpg\",\"url\":\"pas-un-lien\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").exists())
                .andExpect(jsonPath("$.fields.url").exists());
    }

    @Test
    void uploadRejectsNonImages() throws Exception {
        var fake = new MockMultipartFile("file", "virus.jpg", "image/jpeg", "<?php echo 'hack'; ?>".getBytes());
        mvc.perform(multipart("/api/admin/uploads").file(fake).with(user("maceun").roles("ADMIN")).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_IMAGE"));
    }

    @Test
    void uploadAcceptsPng() throws Exception {
        // PNG 1x1 valide
        byte[] png = java.util.Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
        mvc.perform(multipart("/api/admin/photos").file(new MockMultipartFile("file", "a.png", "image/png", png))
                        .with(user("maceun").roles("ADMIN")).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.src").value(org.hamcrest.Matchers.startsWith("/uploads/")))
                .andExpect(jsonPath("$.shape").value("wide"));
    }
}
