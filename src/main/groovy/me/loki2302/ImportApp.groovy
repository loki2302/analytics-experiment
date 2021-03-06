package me.loki2302

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.eclipse.egit.github.core.Commit
import org.eclipse.egit.github.core.CommitUser
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.RepositoryCommit
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.service.RepositoryService

class ImportApp {
    static void main(String[] args) {
        runImport()
    }

    static void runImport() {
        def gitHubUsername = System.properties.getProperty('gitHubUsername')
        def gitHubPassword = System.properties.getProperty('gitHubPassword')
        println gitHubUsername
        println gitHubPassword

        loadGitHubData(gitHubUsername, gitHubPassword)
    }

    static void loadGitHubData(String gitHubUsername, String gitHubPassword) {
        def objectMapper = new ObjectMapper()
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        def jsonFactory = new JsonFactory()
        def jsonGenerator = jsonFactory.createGenerator(new FileWriter('loki2302-github.json'))

        try {
            GitHubClient gitHubClient = new GitHubClient()
            gitHubClient.setCredentials(gitHubUsername, gitHubPassword)

            CommitService commitService = new CommitService(gitHubClient)
            RepositoryService repositoryService = new RepositoryService(gitHubClient)
            List<Repository> repositories = repositoryService.getRepositories()

            jsonGenerator.writeStartObject()
            jsonGenerator.writeFieldName('repositories')
            jsonGenerator.writeStartArray()

            for(Repository repository : repositories) {
                if(repository.private) {
                    continue
                }

                jsonGenerator.writeStartObject()
                jsonGenerator.writeFieldName('fields')
                objectMapper.writeValue(jsonGenerator, GitHubRepository.builder()
                        .name(repository.name)
                        .description(repository.description)
                        .build());

                jsonGenerator.writeFieldName('commits')
                jsonGenerator.writeStartArray()

                List<RepositoryCommit> repositoryCommits = commitService.getCommits(repository)
                for(RepositoryCommit repositoryCommit : repositoryCommits) {
                    repositoryCommit = commitService.getCommit(repository, repositoryCommit.getSha())

                    Commit commit = repositoryCommit.getCommit()
                    CommitUser commitUser = commit.getAuthor()

                    objectMapper.writeValue(jsonGenerator, GitHubCommit.builder()
                        .sha(repositoryCommit.sha)
                        .message(commit.message)
                        .date(commitUser.date)
                        .build())
                }

                jsonGenerator.writeEndArray()
                jsonGenerator.writeEndObject()

                // break
            }

            jsonGenerator.writeEndArray()
            jsonGenerator.writeEndObject()
        } finally {
            jsonGenerator.close()
        }
    }
}
