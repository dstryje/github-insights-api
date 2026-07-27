package com.stryjewski.githubinsights.mapper;

import com.stryjewski.githubinsights.dto.github.GithubRepoDto;
import com.stryjewski.githubinsights.dto.github.GithubUserDto;
import com.stryjewski.githubinsights.dto.response.RepoResponseDto;
import com.stryjewski.githubinsights.dto.response.UserProfileResponseDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class GithubMapper {
    public UserProfileResponseDto mapToUserProfile(GithubUserDto user, List<GithubRepoDto> repos) {
        List<RepoResponseDto> mappedRepos = repos.stream()
                .map(this::mapRepo)
                .toList();

        return UserProfileResponseDto.builder()
                .userName(user.getLogin())
                .displayName(user.getName())
                .geoLocation(user.getLocation())
                .avatar(user.getAvatarUrl())
                .email(user.getEmail())
                .url(user.getUrl())
                .createdAt(formatCreatedAt(user.getCreatedAt()))
                .repos(mappedRepos)
                .build();
    }

    private RepoResponseDto mapRepo(GithubRepoDto githubRepoDto) {
        return RepoResponseDto.builder()
                .name(githubRepoDto.getName())
                .url(githubRepoDto.getUrl())
                .build();
    }

    private String formatCreatedAt(String createdAt) {
        return DateTimeFormatter
                .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)
                .withZone(ZoneOffset.UTC)
                .format(Instant.parse(createdAt));
    }
}
