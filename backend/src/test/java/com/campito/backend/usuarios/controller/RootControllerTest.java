package com.campito.backend.usuarios.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.campito.backend.common.test.BaseWebMvcTest;

/**
 * Tests for RootController.
 * NOTE: With addFilters=false, the SecurityContextHolder is not populated from the request,
 * so the Authentication parameter is always null → always redirects to /login.html.
 */
@WebMvcTest(RootController.class)
@AutoConfigureMockMvc(addFilters = false)
class RootControllerTest extends BaseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void root_sinAutenticacion_redirigeALogin() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login.html"));
    }
}
