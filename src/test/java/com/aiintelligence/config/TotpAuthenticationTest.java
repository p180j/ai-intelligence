package com.aiintelligence.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "admin.totp-enabled=true")
@AutoConfigureMockMvc
class TotpAuthenticationTest {
    @Autowired MockMvc mvc;
    @Autowired TotpVerifier totpVerifier;

    @Test
    void requiresPasswordAndCurrentTotpCode() throws Exception {
        mvc.perform(get("/api/public/login-config")).andExpect(status().isOk())
                .andExpect(jsonPath("$.totpEnabled").value(true));
        mvc.perform(post("/login").param("username", "admin").param("password", "admin").param("totp", "12345"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?error"));

        MvcResult result = mvc.perform(post("/login").param("username", "admin").param("password", "admin")
                        .param("totp", totpVerifier.currentCode()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin")).andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        assertNotNull(session);
        mvc.perform(get("/admin").session(session)).andExpect(status().isOk());
    }
}
