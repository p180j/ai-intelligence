package com.aiintelligence;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties={"admin.totp-enabled=false","admin.totp-secret="}) @AutoConfigureMockMvc
class SecurityAccessTest {
    @Autowired MockMvc mvc;
    @Test void adminRequiresLoginButPublicHomeDoesNot()throws Exception{mvc.perform(get("/admin")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));mvc.perform(get("/")).andExpect(status().isOk());}
    @Test void passwordOnlyLoginWorksWhenTotpIsDisabled()throws Exception{mvc.perform(get("/api/public/login-config")).andExpect(status().isOk()).andExpect(jsonPath("$.totpEnabled").value(false));mvc.perform(post("/login").param("username","admin").param("password","admin")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin"));}
}
