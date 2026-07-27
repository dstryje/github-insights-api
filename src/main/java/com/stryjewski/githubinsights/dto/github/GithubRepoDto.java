package com.stryjewski.githubinsights.dto.github;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubRepoDto {
    private String name;

    private String url;
}
