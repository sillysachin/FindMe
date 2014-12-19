package com.appbootup.findme;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;

public interface GitHubService {
    @GET("/users/{user}/repos")
    List<Repo> listRepos(@Path("user") String user);
}