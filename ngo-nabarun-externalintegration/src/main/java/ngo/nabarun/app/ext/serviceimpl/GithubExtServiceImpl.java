package ngo.nabarun.app.ext.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ngo.nabarun.app.common.helper.PropertyHelper;
import ngo.nabarun.app.ext.objects.GitHubResponse;
import ngo.nabarun.app.ext.objects.GitHubResponse.Discussion;
import ngo.nabarun.app.ext.service.IGitHubExtService;

@Service
public class GithubExtServiceImpl implements IGitHubExtService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private PropertyHelper propertyHelper;

	@Override
	@Cacheable(value = "GH_discussions", key = "#discussionId")
	public Discussion getGitHubDiscussion(String owner, String repo, String discussionId) {
		String githubToken = propertyHelper.getGithubToken();
		String query = "{\r\n" + "    \"query\":\"{ repository(owner: \\\"" + owner + "\\\", name: \\\"" + repo
				+ "\\\") { discussion(number: " + discussionId + ") { id title bodyHTML } } }\"\r\n" + "}";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + githubToken);
		headers.set("Content-Type", "application/json");
		HttpEntity<String> entity = new HttpEntity<>(query, headers);

		GitHubResponse response = restTemplate
				.exchange("https://api.github.com/graphql", HttpMethod.POST, entity, GitHubResponse.class).getBody();
		return response.getData().getRepository().getDiscussion();
	}

}
