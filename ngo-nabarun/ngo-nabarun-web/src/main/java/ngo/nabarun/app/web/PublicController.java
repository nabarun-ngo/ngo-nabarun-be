package ngo.nabarun.app.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import ngo.nabarun.app.businesslogic.IPublicBL;
import ngo.nabarun.app.businesslogic.IUserBL;
import ngo.nabarun.app.businesslogic.businessobjects.SignUpDetail;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.exception.BusinessException;

@Controller
@SessionAttributes(names = { "interview"})
public class PublicController {

	@Autowired
	private IUserBL userBl;

	@Autowired
	private IPublicBL publicBl;

	@GetMapping({"/","/signup","/contact"})
	public String homePage(Model model) {
		List<UserDetail> users = userBl.getPublicProfiles();
		model.addAttribute("profiles", users);
		model.addAttribute("interview", new SignUpDetail());
		try {
			for(KeyValue keyValue:publicBl.getOrganizationInfo()) {
				model.addAttribute(keyValue.getKey(), keyValue.getValue());
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "index";
	}

	@PostMapping("/signup")
	public ModelAndView signUp(@ModelAttribute("interview") SignUpDetail interview) throws Exception {
		ModelAndView modelAndView = new ModelAndView("pages/joining-rule-otp");
		modelAndView.addObject("interview", interview);
		try {
			interview = publicBl.signUp(interview);
			modelAndView.addObject("stage", interview.getStageId());
			modelAndView.addObject("successMessage", interview.getMessage());
			modelAndView.addObject("pageName", interview.getBreadCrumb().get(interview.getBreadCrumb().size() - 1));
			modelAndView.addObject("breadcrumb", interview.getBreadCrumb());
			modelAndView.addObject("rules", interview.getRules());
			modelAndView.addObject("siteKey", interview.getSiteKey());
		} catch (BusinessException e) {
			modelAndView.addObject("stage", interview.getStageId());
			modelAndView.addObject("errorMessage", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			modelAndView.addObject("successMessage", interview.getMessage());
			modelAndView.addObject("pageName", interview.getBreadCrumb().get(interview.getBreadCrumb().size() - 1));
			modelAndView.addObject("breadcrumb", interview.getBreadCrumb());
		}
		return modelAndView;
	}
	
	@PostMapping("/contact")
	public ModelAndView contactUs(@ModelAttribute("interview") SignUpDetail interview) throws Exception {
		ModelAndView modelAndView = new ModelAndView("pages/joining-rule-otp");
		modelAndView.addObject("interview", interview);
		return modelAndView;
	}
	
}
