package com.stryjewski.githubinsights.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.List;

@JsonPropertyOrder({
        "user_name",
        "display_name",
        "avatar",
        "geo_location",
        "email",
        "url",
        "created_at",
        "repos"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponseDto {
    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("display_name")
    private String displayName;

    private String avatar;

    @JsonProperty("geo_location")
    private String geoLocation;

    private String email;

    private String url;

    @JsonProperty("created_at")
    private String createdAt;

    private List<RepoResponseDto> repos;
}
