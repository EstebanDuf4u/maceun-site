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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.maceun.api.release.source.DeezerClient;
import fr.maceun.api.release.source.DeezerInfo;
import fr.maceun.api.release.source.LinkException;
import fr.maceun.api.release.source.LinkMetadata;
import fr.maceun.api.release.source.LinkScraper;
import fr.maceun.api.release.source.Platform;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiTest {

    @Autowired
    MockMvc mvc;

    @MockitoSpyBean
    LinkScraper scraper;

    @MockitoBean
    DeezerClient deezer;

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
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://maceun.fanlink.tv/x\"}"))
                .andExpect(status().isForbidden());
    }

    // ----- admin -----



    @Test
    void uploadRejectsNonImages() throws Exception {
        var fake = new MockMultipartFile("file", "virus.jpg", "image/jpeg", "<?php echo 'hack'; ?>".getBytes());
        mvc.perform(multipart("/api/admin/photos").file(fake).with(user("maceun").roles("ADMIN")).with(csrf()))
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

    // ----- sons (Fanlink + Deezer simulés) -----

    @Test
    void addReleaseFromFanlinkLink() throws Exception {
        var url = "https://maceun.fanlink.tv/nouveau-son";
        doReturn(new LinkMetadata("Nouveau Son", "https://i.scdn.co/cover.jpg", "FR1234567890",
                List.of(new Platform("Spotify", "https://open.spotify.com/track/1"))))
                .when(scraper).fetch(url);
        when(deezer.find("FR1234567890", "Nouveau Son")).thenReturn(Optional.of(
                new DeezerInfo(LocalDate.of(2027, 2, 1), "https://cdn.deezer/cover_xl.jpg", "Techno", "https://www.deezer.com/track/9")));
        var admin = user("maceun").roles("ADMIN");

        var body = mvc.perform(post("/api/admin/releases").with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"" + url + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Nouveau Son"))
                .andExpect(jsonPath("$.releaseDate").value("2027-02-01"))
                .andExpect(jsonPath("$.cover").value("https://cdn.deezer/cover_xl.jpg"))
                .andExpect(jsonPath("$.platforms[1].name").value("Deezer"))
                .andReturn().getResponse().getContentAsString();
        var id = body.replaceAll("^\\{\"id\":(\\d+).*", "$1");

        // le plus récent devient la sortie mise en avant
        mvc.perform(get("/api/releases")).andExpect(jsonPath("$.latest.title").value("Nouveau Son"))
                .andExpect(jsonPath("$.latest.platforms[0].name").value("Spotify"));

        // doublon refusé
        mvc.perform(post("/api/admin/releases").with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"" + url + "\"}"))
                .andExpect(status().isBadRequest());

        // masqué → n'apparaît plus sur le site
        mvc.perform(put("/api/admin/releases/" + id).with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"visible\":false}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/releases")).andExpect(jsonPath("$.latest.title").value(org.hamcrest.Matchers.not("Nouveau Son")));

        mvc.perform(delete("/api/admin/releases/" + id).with(admin).with(csrf())).andExpect(status().isNoContent());
    }

    @Test
    void unreadableOrForeignLinksAreRefused() throws Exception {
        var admin = user("maceun").roles("ADMIN");
        mvc.perform(post("/api/admin/releases").with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://evil.example/x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_LINK"));

        var dead = "https://maceun.fanlink.tv/lien-mort";
        doThrow(new LinkException("La page du lien répond « 404 » (lien mort ?).")).when(scraper).fetch(dead);
        mvc.perform(post("/api/admin/releases").with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"" + dead + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("404")));
        mvc.perform(get("/api/admin/releases").with(admin))
                .andExpect(jsonPath("$[?(@.url == '" + dead + "')]").isEmpty());
    }

    @Test
    void failedSyncKeepsLastKnownInfo() throws Exception {
        // les sons de départ (migration) : un échec de mise à jour ne doit rien effacer
        doThrow(new LinkException("Page du lien injoignable.")).when(scraper).fetch(org.mockito.ArgumentMatchers.anyString());
        var admin = user("maceun").roles("ADMIN");
        mvc.perform(post("/api/admin/releases/1/sync").with(admin).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Feel Your Love"))
                .andExpect(jsonPath("$.syncError").value("Page du lien injoignable."));
    }

    // ----- épingler / modifier -----

    @Test
    void pinnedReleaseIsFeaturedInsteadOfNewest() throws Exception {
        var admin = user("maceun").roles("ADMIN");
        // son 1 (Feel Your Love, le plus ancien) épinglé → mis en avant
        mvc.perform(put("/api/admin/releases/1/pin").with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"pinned\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 1)].pinned").value(true));
        mvc.perform(get("/api/releases"))
                .andExpect(jsonPath("$.latest.id").value(1))
                .andExpect(jsonPath("$.latest.number").value(1))          // numéro inchangé
                .andExpect(jsonPath("$.previous[0].title").value("I Lose Myself"));

        // un seul épinglé à la fois
        mvc.perform(put("/api/admin/releases/2/pin").with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"pinned\":true}"))
                .andExpect(jsonPath("$[?(@.pinned == true)].id").value(org.hamcrest.Matchers.contains(2)));

        // désépingler → retour au plus récent
        mvc.perform(put("/api/admin/releases/2/pin").with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"pinned\":false}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/releases")).andExpect(jsonPath("$.latest.title").value("I Lose Myself"));
    }

    @Test
    void editReleaseLink() throws Exception {
        var admin = user("maceun").roles("ADMIN");
        var newUrl = "https://maceun.fanlink.tv/safe-and-sound-v2";
        doReturn(new LinkMetadata("Safe And Sound", null, null, List.of())).when(scraper).fetch(newUrl);

        mvc.perform(put("/api/admin/releases/2").with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"" + newUrl + "\",\"visible\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(newUrl));

        // lien mort → refusé, l'ancien lien est gardé
        var dead = "https://maceun.fanlink.tv/mort";
        doThrow(new LinkException("La page du lien répond « 404 » (lien mort ?).")).when(scraper).fetch(dead);
        mvc.perform(put("/api/admin/releases/2").with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"" + dead + "\",\"visible\":true}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/releases").with(admin))
                .andExpect(jsonPath("$[?(@.id == 2)].url").value(newUrl));

        // lien déjà utilisé par un autre son → refusé
        mvc.perform(put("/api/admin/releases/2").with(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://maceun.fanlink.tv/feel-your-love\",\"visible\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ce lien est déjà utilisé par un autre son."));
    }
}
