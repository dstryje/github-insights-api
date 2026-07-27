package com.stryjewski.githubinsights.controller;

import com.stryjewski.githubinsights.dto.response.UserProfileResponseDto;
import com.stryjewski.githubinsights.service.GithubUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/github")
@RequiredArgsConstructor
public class GithubUserController {
    private final GithubUserService githubUserService;

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponseDto> getUserProfile(@PathVariable String username) {
        return ResponseEntity.ok(githubUserService.getUserProfile(username));
    }
}
