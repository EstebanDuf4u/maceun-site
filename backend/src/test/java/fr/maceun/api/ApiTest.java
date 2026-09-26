package fr.maceun.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void releasesReturnsLatestFirst() throws Exception {
        mvc.perform(get("/api/releases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latest.title").value("I Lose Myself"))
                .andExpect(jsonPath("$.previous.length()").value(4));
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
}
