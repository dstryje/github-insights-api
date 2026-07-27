package com.stryjewski.githubinsights.exception;

public class GithubApiException extends RuntimeException {
    public GithubApiException(String message) {
        super(message);
    }
}
