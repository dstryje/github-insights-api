package com.stryjewski.githubinsights.mapper;

import com.stryjewski.githubinsights.dto.github.GithubRepoDto;
import com.stryjewski.githubinsights.dto.github.GithubUserDto;
import com.stryjewski.githubinsights.dto.response.UserProfileResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GithubMapperTest {

    private GithubMapper githubMapper;

    @BeforeEach
    void setUp() {
        githubMapper = new GithubMapper();
    }

    @Test
    void mapToUserProfile_shouldMapGithubUserAndReposToResponse() {
        GithubUserDto user = GithubUserDto.builder()
                .login("octocat")
                .name("The Octocat")
                .avatarUrl("avatar-url")
                .location("San Francisco")
                .email(null)
                .url("https://api.github.com/users/octocat")
                .createdAt("2026-06-30T18:44:36Z")
                .build();

        List<GithubRepoDto> repos =
                List.of(GithubRepoDto.builder()
                        .name("repo-one")
                        .url("repo-url-one")
                        .build());

        UserProfileResponseDto response = githubMapper.mapToUserProfile(user, repos);
        assertEquals("octocat", response.getUserName());
        assertEquals("The Octocat", response.getDisplayName());
        assertEquals("avatar-url", response.getAvatar());
        assertEquals("San Francisco", response.getGeoLocation());
        assertNull(response.getEmail());
        assertEquals("https://api.github.com/users/octocat", response.getUrl());
        assertEquals("Tue, 30 Jun 2026 18:44:36 GMT", response.getCreatedAt());
        assertEquals(1, response.getRepos().size());
        assertEquals("repo-one", response.getRepos().get(0).getName());
        assertEquals("repo-url-one", response.getRepos().get(0).getUrl());
    }
}