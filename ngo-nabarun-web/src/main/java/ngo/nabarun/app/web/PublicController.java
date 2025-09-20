package ngo.nabarun.app.web;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;

import ngo.nabarun.app.businesslogic.IPublicBL;
import ngo.nabarun.app.businesslogic.businessobjects.InterviewDetail;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.common.helper.PropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;

@Controller
@SessionAttributes(names = { "interview" })
public class PublicController {

	private static Map<String, Object> content;

	@Autowired
	private IPublicBL publicBl;

	public PublicController(PropertyHelper prop) {
		content = loadContent(prop);
	}

	@GetMapping({ "/", "/signup", "/contact", "/donate" })
	public String homePage(Model model) {
		Map<String, Object> pageDataMap = publicBl.getPageData(List.of("profiles"));
		for (Entry<String, Object> data : pageDataMap.entrySet()) {
			model.addAttribute(data.getKey(), data.getValue());
		}
		model.addAttribute("interview", new InterviewDetail());
		model.addAttribute("VERSION", CommonUtils.getAppVersion());
		model.addAttribute("content", content);
		return "index";
	}

	@GetMapping("/policy/{page:(?:privacy-policy|terms-of-use|disclaimer|copyright)}")
	public ModelAndView policy(@PathVariable String page) throws Exception {
		ModelAndView modelAndView = new ModelAndView("policy");
		Map<String, Object> pageDataMap = publicBl.getPageData(List.of("policy"));
		for (Entry<String, Object> data : pageDataMap.entrySet()) {
			modelAndView.addObject(data.getKey(), data.getValue());
		}
		switch (page) {
		case "privacy-policy":
			modelAndView.addObject("pageName", "Privacy Policy");
			modelAndView.addObject("breadcrumb", List.of("Home", "Privacy Policy"));
			modelAndView.addObject("description",
					"This Privacy Policy outlines how we handle your personal information.");
			modelAndView.addObject("url", pageDataMap.get("POLICY_PRIVACY_POLICY"));
			break;
		case "terms-of-use":
			modelAndView.addObject("pageName", "Terms of Use");
			modelAndView.addObject("breadcrumb", List.of("Home", "Terms of Use"));
			modelAndView.addObject("description", "These Terms of Use govern your use of our website and services.");
			modelAndView.addObject("url", pageDataMap.get("POLICY_TERMS_OF_USE"));
			break;
		case "disclaimer":
			modelAndView.addObject("pageName", "Disclaimer");
			modelAndView.addObject("breadcrumb", List.of("Home", "Disclaimer"));
			modelAndView.addObject("description", "This Disclaimer outlines the limitations of our liability.");
			modelAndView.addObject("url", pageDataMap.get("POLICY_DISCLAIMER"));
			break;
		case "copyright":
			modelAndView.addObject("pageName", "Copyright");
			modelAndView.addObject("breadcrumb", List.of("Home", "Copyright"));
			modelAndView.addObject("description",
					"This Copyright notice outlines the ownership of content on our website.");
			modelAndView.addObject("url", pageDataMap.get("POLICY_COPYRIGHT"));
			break;
		default:
			throw new BusinessException("Invalid page requested: " + page);
		}
		modelAndView.addObject("content", content);
		return modelAndView;
	}

	@PostMapping("/signup")
	public ModelAndView signUp(@ModelAttribute("interview") InterviewDetail interview) throws Exception {
		ModelAndView modelAndView = new ModelAndView("stages");
		modelAndView.addObject("interview", interview);
		try {
			interview = publicBl.signUp(interview);
			modelAndView.addObject("stage", interview.getStage());
			modelAndView.addObject("successMessage", interview.getMessage());
			modelAndView.addObject("pageName", interview.getBreadCrumb().get(interview.getBreadCrumb().size() - 1));
			modelAndView.addObject("breadcrumb", interview.getBreadCrumb());
			modelAndView.addObject("rules", interview.getRules());
			modelAndView.addObject("rulesHTML", interview.getRulesHTML());
			modelAndView.addObject("siteKey", interview.getSiteKey());
		} catch (BusinessException e) {
			modelAndView.addObject("stage", interview.getStage());
			modelAndView.addObject("errorMessage", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			modelAndView.addObject("successMessage", interview.getMessage());
			modelAndView.addObject("pageName", interview.getBreadCrumb().get(interview.getBreadCrumb().size() - 1));
			modelAndView.addObject("breadcrumb", interview.getBreadCrumb());
		}
		Map<String, Object> pageDataMap = publicBl.getPageData(List.of("policy"));
		for (Entry<String, Object> data : pageDataMap.entrySet()) {
			modelAndView.addObject(data.getKey(), data.getValue());
		}
		modelAndView.addObject("content", content);
		return modelAndView;
	}

	@PostMapping("/contact")
	public ModelAndView contactUs(@ModelAttribute("interview") InterviewDetail interview) throws Exception {
		ModelAndView modelAndView = new ModelAndView("stages");
		modelAndView.addObject("interview", interview);
		try {
			interview = publicBl.contact(interview);
			modelAndView.addObject("stage", interview.getStage());
			modelAndView.addObject("successMessage", interview.getMessage());
			modelAndView.addObject("pageName", interview.getBreadCrumb().get(interview.getBreadCrumb().size() - 1));
			modelAndView.addObject("breadcrumb", interview.getBreadCrumb());
			modelAndView.addObject("rules", interview.getRules());
			modelAndView.addObject("siteKey", interview.getSiteKey());
		} catch (BusinessException e) {
			modelAndView.addObject("stage", interview.getStage());
			modelAndView.addObject("errorMessage", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			modelAndView.addObject("successMessage", interview.getMessage());
			modelAndView.addObject("pageName", interview.getBreadCrumb().get(interview.getBreadCrumb().size() - 1));
			modelAndView.addObject("breadcrumb", interview.getBreadCrumb());
		}
		Map<String, Object> pageDataMap = publicBl.getPageData(List.of());
		for (Entry<String, Object> data : pageDataMap.entrySet()) {
			modelAndView.addObject(data.getKey(), data.getValue());
		}
		modelAndView.addObject("content", content);
		return modelAndView;
	}

	@PostMapping(path = "/donate")
	public ModelAndView donate(@ModelAttribute("interview") InterviewDetail interview) throws Exception {
		// System.out.println(files);
		ModelAndView modelAndView = new ModelAndView("stages");
		modelAndView.addObject("interview", interview);
		try {
			interview = publicBl.initDonation(interview);
			modelAndView.addObject("stage", interview.getStage());
			modelAndView.addObject("successMessage", interview.getMessage());
			modelAndView.addObject("pageName", interview.getBreadCrumb().get(interview.getBreadCrumb().size() - 1));
			modelAndView.addObject("breadcrumb", interview.getBreadCrumb());
			modelAndView.addObject("rules", interview.getRules());
			modelAndView.addObject("siteKey", interview.getSiteKey());
		} catch (BusinessException e) {
			e.printStackTrace();
			modelAndView.addObject("stage", interview.getStage());
			modelAndView.addObject("errorMessage", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			modelAndView.addObject("successMessage", interview.getMessage());
			modelAndView.addObject("pageName", interview.getBreadCrumb().get(interview.getBreadCrumb().size() - 1));
			modelAndView.addObject("breadcrumb", interview.getBreadCrumb());
		}
		Map<String, Object> pageDataMap = publicBl.getPageData(List.of());
		for (Entry<String, Object> data : pageDataMap.entrySet()) {
			modelAndView.addObject(data.getKey(), data.getValue());
		}
		modelAndView.addObject("content", content);
		return modelAndView;
	}

	private Map<String, Object> loadContent(PropertyHelper prop) {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		Resource resource = resourceLoader.getResource("classpath:content.json");
		try (InputStream inputStream = resource.getInputStream()) {
			String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			Map<String, String> replacements = Map.of("##LOGIN_URL##", prop.getAppLoginURL());
			for (var entry : replacements.entrySet()) {
				json = json.replace(entry.getKey(), entry.getValue());
			}
			return CommonUtils.getObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			throw new RuntimeException("Failed to load content.json file", e);
		}
	}
}
