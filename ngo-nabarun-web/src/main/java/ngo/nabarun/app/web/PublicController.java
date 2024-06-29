package ngo.nabarun.app.web;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import ngo.nabarun.app.businesslogic.IPublicBL;
import ngo.nabarun.app.businesslogic.businessobjects.InterviewDetail;
import ngo.nabarun.app.businesslogic.exception.BusinessException;

@Controller
@SessionAttributes(names = { "interview"})
public class PublicController {

	@Autowired
	private IPublicBL publicBl;

	@GetMapping({"/","/signup","/contact","/donate"})
	public String homePage(Model model) {
		Map<String, Object> pageDataMap=publicBl.getPageData();
		for(Entry<String, Object> data:pageDataMap.entrySet()) {
			model.addAttribute(data.getKey(), data.getValue());
		}
		model.addAttribute("interview", new InterviewDetail());
		return "index";
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
			modelAndView.addObject("siteKey", interview.getSiteKey());
		} catch (BusinessException e) {
			modelAndView.addObject("stage", interview.getStage());
			modelAndView.addObject("errorMessage", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			modelAndView.addObject("successMessage", interview.getMessage());
			modelAndView.addObject("pageName", interview.getBreadCrumb().get(interview.getBreadCrumb().size() - 1));
			modelAndView.addObject("breadcrumb", interview.getBreadCrumb());
		}
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
		return modelAndView;
	}
	
	@PostMapping("/donate")
	public ModelAndView donate(@ModelAttribute("interview") InterviewDetail interview) throws Exception {
		
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
			modelAndView.addObject("stage", interview.getStage());
			modelAndView.addObject("errorMessage", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			modelAndView.addObject("successMessage", interview.getMessage());
			modelAndView.addObject("pageName", interview.getBreadCrumb().get(interview.getBreadCrumb().size() - 1));
			modelAndView.addObject("breadcrumb", interview.getBreadCrumb());
		}
		return modelAndView;
	}

	
}
