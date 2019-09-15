package com.github.wslf.codeforces.storage.github;

import com.github.wslf.codeforces.storage.StorageException;
import com.github.wslf.codeforces.storage.StorageStrategy;
import com.github.wslf.github.Credentials;
import com.github.wslf.github.fileapi.FileAPI;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.IOException;

@Log4j2
@EnableConfigurationProperties(GitHubProperties.class)
public class GitHubStorageStrategy<T> implements StorageStrategy<T, Identifier, OptionalParameters> {

  private final Credentials credentials;
  private final Gson gson;
  private final FileAPI githubAPI;
  private final OptionalParameters defaultSaveParameters;

  public GitHubStorageStrategy(GitHubProperties gitHubProperties) {
    this.credentials = new Credentials(gitHubProperties.getUserName(), gitHubProperties.getUserToken());
    this.gson = new Gson();
    this.githubAPI = new FileAPI();
    this.defaultSaveParameters = new OptionalParameters("save data");
  }

  @Override
  public void save(T data, Identifier identifier, OptionalParameters parameters) throws StorageException {
    String json = gson.toJson(data);
    String commitMessage = (parameters == null || parameters.getCommitMessage() ==
                                                  null) ? defaultSaveParameters.getCommitMessage() : parameters.getCommitMessage();

    try {
      githubAPI.createOrUpdateFile(credentials, identifier.repository, identifier.path, json, commitMessage,
          identifier.branch);
    } catch (IOException e) {
      throw new StorageException(e);
    }
  }

  @Override
  public T load(Identifier identifier, OptionalParameters parameters) throws StorageException {
    String json;
    try {
      json = githubAPI.getFileContent(credentials, identifier.repository, identifier.path, identifier.branch);
    } catch (IOException e) {
      throw new StorageException(e);
    }

    return gson.fromJson(json, identifier.type);
  }
}
