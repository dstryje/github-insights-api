package com.stryjewski.githubinsights.controller;

import com.stryjewski.githubinsights.dto.response.RepoResponseDto;
import com.stryjewski.githubinsights.dto.response.UserProfileResponseDto;
import com.stryjewski.githubinsights.service.GithubUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GithubUserController.class)
class GithubUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GithubUserService githubUserService;

    @Test
    void getUserProfile_shouldReturnUserProfile() throws Exception {
        UserProfileResponseDto response = UserProfileResponseDto.builder()
                .userName("octocat")
                .displayName("The Octocat")
                .avatar("avatar-url")
                .geoLocation("San Francisco")
                .email(null)
                .url("https://api.github.com/users/octocat")
                .createdAt("Tue, 25 Jan 2011 18:44:36 GMT")
                .repos(List.of(
                        RepoResponseDto.builder()
                                .name("repo-one")
                                .url("repo-url-one")
                                .build()
                ))
                .build();

        when(githubUserService.getUserProfile("octocat")).thenReturn(response);

        mockMvc.perform(get("/api/v1/github/octocat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_name").value("octocat"))
                .andExpect(jsonPath("$.display_name").value("The Octocat"))
                .andExpect(jsonPath("$.avatar").value("avatar-url"))
                .andExpect(jsonPath("$.geo_location").value("San Francisco"))
                .andExpect(jsonPath("$.email").isEmpty())
                .andExpect(jsonPath("$.url").value("https://api.github.com/users/octocat"))
                .andExpect(jsonPath("$.created_at").value("Tue, 25 Jan 2011 18:44:36 GMT"))
                .andExpect(jsonPath("$.repos[0].name").value("repo-one"))
                .andExpect(jsonPath("$.repos[0].url").value("repo-url-one"));

        verify(githubUserService).getUserProfile("octocat");
    }
}