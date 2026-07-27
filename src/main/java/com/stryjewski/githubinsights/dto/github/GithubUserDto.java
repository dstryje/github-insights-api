package com.stryjewski.githubinsights.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubUserDto {
    private String login;

    private String name;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String location;

    private String email;

    private String url;

    @JsonProperty("created_at")
    private String createdAt;
}
