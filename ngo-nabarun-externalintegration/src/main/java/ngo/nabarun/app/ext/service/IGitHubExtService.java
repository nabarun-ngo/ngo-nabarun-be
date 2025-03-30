package ngo.nabarun.app.ext.service;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.ext.objects.GitHubResponse.Discussion;

@Service
public interface IGitHubExtService {
	Discussion getGitHubDiscussion(String owner,String  repo,String discussionId);
}
