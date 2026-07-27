package com.stryjewski.githubinsights.client;

import com.stryjewski.githubinsights.dto.github.GithubRepoDto;
import com.stryjewski.githubinsights.dto.github.GithubUserDto;
import com.stryjewski.githubinsights.exception.GithubApiException;
import com.stryjewski.githubinsights.exception.GithubUserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubClientImpl implements GithubClient {
    private final RestClient restClient;

    @Value("${githubapi.user-path}")
    private String userPath;

    @Value("${githubapi.repos-path}")
    private String reposPath;

    @Override
    public GithubUserDto getUser(String username) {
        try {
            return restClient.get()
                    .uri(userPath, username)
                    .retrieve()
                    .body(GithubUserDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new GithubUserNotFoundException("User not found: " + username);

        } catch (Exception ex) {
            throw new GithubApiException("Error retrieving user");
        }
    }

    @Override
    public List<GithubRepoDto> getRepos(String username) {
        try {
            GithubRepoDto[] repos = restClient.get()
                    .uri(reposPath, username)
                    .retrieve()
                    .body(GithubRepoDto[].class);
            if (repos == null) {
                return Collections.emptyList();
            }
            return List.of(repos);

        } catch (Exception ex) {
            throw new GithubApiException("Error retrieving repositories");
        }
    }
}
