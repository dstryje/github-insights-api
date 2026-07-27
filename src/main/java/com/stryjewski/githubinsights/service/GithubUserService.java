package com.stryjewski.githubinsights.service;

import com.stryjewski.githubinsights.client.GithubClient;
import com.stryjewski.githubinsights.dto.github.GithubRepoDto;
import com.stryjewski.githubinsights.dto.github.GithubUserDto;
import com.stryjewski.githubinsights.dto.response.UserProfileResponseDto;
import com.stryjewski.githubinsights.mapper.GithubMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GithubUserService {
    private final GithubClient githubClient;
    private final GithubMapper githubMapper;

    public UserProfileResponseDto getUserProfile(String username) {
        GithubUserDto user = githubClient.getUser(username);
        List<GithubRepoDto> repos = githubClient.getRepos(username);

        return githubMapper.mapToUserProfile(user, repos);
    }
}
