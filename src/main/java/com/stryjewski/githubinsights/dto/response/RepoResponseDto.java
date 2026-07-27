package com.stryjewski.githubinsights.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepoResponseDto {
    private String name;

    private String url;
}