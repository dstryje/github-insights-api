package com.stryjewski.githubinsights.client;

import com.stryjewski.githubinsights.dto.github.GithubRepoDto;
import com.stryjewski.githubinsights.dto.github.GithubUserDto;

import java.util.List;

public interface GithubClient {
    GithubUserDto getUser(String username);

    List<GithubRepoDto> getRepos(String username);
}
