package ngo.nabarun.app.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ngo.nabarun.app.businesslogic.IUserBL;
import ngo.nabarun.app.businesslogic.businessobjects.JoiningInterviewDetail;
import ngo.nabarun.app.businesslogic.businessobjects.Page;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;


@Controller
public class PublicController {
	
	@Autowired
	private IUserBL userBl;
	
	@GetMapping
	public String homePage(Model model) {
		System.out.println("hello");
		Page<UserDetail> users=userBl.getAllUser(null, null, null);
		model.addAttribute("profiles", users.getContent());
		model.addAttribute("interview", new JoiningInterviewDetail());
		model.addAttribute("loginURL", "");
		return "index";
	}


//	@PostMapping("/join/rules") 
//	public ModelAndView showRulesAndRegulation(
//			@ModelAttribute("interview") RegistrationObject interview)
//			throws Exception {
//		ModelAndView modelAndView = new ModelAndView("pages/joining-rule-otp");
//		modelAndView.addObject("interview", interview);
//		modelAndView.addObject("stage", 1);
//		modelAndView.addObject("pageName", "Rules and Regulations");
//		return modelAndView;
//
//	}
//	@PostMapping("/join/verify-email")
//	public ModelAndView register(
//			@ModelAttribute("interview") RegistrationObject interview
//			)
//			throws Exception {
//		ModelAndView modelAndView = new ModelAndView("pages/joining-rule-otp");
//
//		OtpObject otpObject=new OtpObject();
//		otpObject.setName(interview.getFirstName()+" "+interview.getLastName());
//		otpObject.setEmail(interview.getEmail());
//		otpObject=registrationService.requestRegistrationOtp(otpObject);
//		modelAndView.addObject("otpInfo", otpObject);
//		modelAndView.addObject("stage", 2);
//		modelAndView.addObject("pageName", "Verify Email");
//		return modelAndView;
//
//
//	}
//	
//	@PostMapping("/join/register")
//	public ModelAndView register(
//			@ModelAttribute("interview") RegistrationObject interview,
//			@ModelAttribute("otpInfo") OtpObject otpObject
//			)
//			throws Exception {
//		ModelAndView modelAndView = new ModelAndView("pages/joining-rule-otp");
//
//		try {
//		String requestId=registrationService.register(interview,otpObject);
//		modelAndView.addObject("stage", 3);
//		modelAndView.addObject("message", contentStore.getRemoteMessage(MessageKey.SUCCESS_MSG_JOIN_REQUEST_CREATED, Map.of("id", requestId)));
//		registrationService.invalidateRegistrationOtp(otpObject.getOtpServicetoken());
//		modelAndView.addObject("pageName", "Success");
//		}catch (Exception e) {
//			e.printStackTrace();
//			modelAndView.addObject("stage", 2);
//			modelAndView.addObject("errorMessage", e.getCause() !=null ? e.getCause().getMessage():e.getMessage());
//			modelAndView.addObject("pageName", "Verify Email");
//		}
//		return modelAndView;
//
//
//	}
//	
//	@PostMapping("/join/resend-email")
//	public ModelAndView resendOTP(
//			@ModelAttribute("otpInfo") OtpObject otpObject
//			)
//			throws Exception {
//		ModelAndView modelAndView = new ModelAndView("pages/joining-rule-otp");
//		registrationService.resendRegistrationOtp(otpObject.getOtpServicetoken());
//		modelAndView.addObject("stage", 2);
//		modelAndView.addObject("successMessage", "One Time Password successfully sent.");
//		return modelAndView;
//
//
//	}
}
