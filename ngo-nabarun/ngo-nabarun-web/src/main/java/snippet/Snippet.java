//package snippet;
//
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.servlet.ModelAndView;
//
//public class Snippet {
//	@GetMapping
//		public String homePage(Model model) {
//			List<ProfileSummaryObject> profile=profileService.getPublicProfileSummary();
//			model.addAttribute("profiles", profile);
//			model.addAttribute("interview", new RegistrationObject());
//			model.addAttribute("loginURL", propertyStore.getProperty(Key.APP_LOGIN_URL));
//			return "index";
//		}
//	
//	
//		@PostMapping("/join/rules") 
//		public ModelAndView showRulesAndRegulation(
//				@ModelAttribute("interview") RegistrationObject interview)
//				throws Exception {
//			ModelAndView modelAndView = new ModelAndView("pages/joining-rule-otp");
//			modelAndView.addObject("interview", interview);
//			modelAndView.addObject("stage", 1);
//			modelAndView.addObject("pageName", "Rules and Regulations");
//			return modelAndView;
//	
//		}
//		@PostMapping("/join/verify-email")
//		public ModelAndView register(
//				@ModelAttribute("interview") RegistrationObject interview
//				)
//				throws Exception {
//			ModelAndView modelAndView = new ModelAndView("pages/joining-rule-otp");
//	
//			OtpObject otpObject=new OtpObject();
//			otpObject.setName(interview.getFirstName()+" "+interview.getLastName());
//			otpObject.setEmail(interview.getEmail());
//			otpObject=registrationService.requestRegistrationOtp(otpObject);
//			modelAndView.addObject("otpInfo", otpObject);
//			modelAndView.addObject("stage", 2);
//			modelAndView.addObject("pageName", "Verify Email");
//			return modelAndView;
//	
////			<!-- Messenger Chat plugin Code -->
////		    <div id="fb-root"></div>
////
////		    <!-- Your Chat plugin code -->
////		    <div id="fb-customer-chat" class="fb-customerchat">
////		    </div>
////
////		    <script>
////		      var chatbox = document.getElementById('fb-customer-chat');
////		      chatbox.setAttribute("page_id", "697686966927006");
////		      chatbox.setAttribute("attribution", "biz_inbox");
////		    </script>
////
////		    <!-- Your SDK code -->
////		    <script>
////		      window.fbAsyncInit = function() {
////		        FB.init({
////		          xfbml            : true,
////		          version          : 'v18.0'
////		        });
////		      };
////
////		      (function(d, s, id) {
////		        var js, fjs = d.getElementsByTagName(s)[0];
////		        if (d.getElementById(id)) return;
////		        js = d.createElement(s); js.id = id;
////		        js.src = 'https://connect.facebook.net/en_US/sdk/xfbml.customerchat.js';
////		        fjs.parentNode.insertBefore(js, fjs);
////		      }(document, 'script', 'facebook-jssdk'));
////		    </script>
//		}
//		
//		@PostMapping("/join/register")
//		public ModelAndView register(
//				@ModelAttribute("interview") RegistrationObject interview,
//				@ModelAttribute("otpInfo") OtpObject otpObject
//				)
//				throws Exception {
//			ModelAndView modelAndView = new ModelAndView("pages/joining-rule-otp");
//	
//			try {
//			String requestId=registrationService.register(interview,otpObject);
//			modelAndView.addObject("stage", 3);
//			modelAndView.addObject("message", contentStore.getRemoteMessage(MessageKey.SUCCESS_MSG_JOIN_REQUEST_CREATED, Map.of("id", requestId)));
//			registrationService.invalidateRegistrationOtp(otpObject.getOtpServicetoken());
//			modelAndView.addObject("pageName", "Success");
//			}catch (Exception e) {
//				e.printStackTrace();
//				modelAndView.addObject("stage", 2);
//				modelAndView.addObject("errorMessage", e.getCause() !=null ? e.getCause().getMessage():e.getMessage());
//				modelAndView.addObject("pageName", "Verify Email");
//			}
//			return modelAndView;
//	
//	
//		}
//		
//		@PostMapping("/join/resend-email")
//		public ModelAndView resendOTP(
//				@ModelAttribute("otpInfo") OtpObject otpObject
//				)
//				throws Exception {
//			ModelAndView modelAndView = new ModelAndView("pages/joining-rule-otp");
//			registrationService.resendRegistrationOtp(otpObject.getOtpServicetoken());
//			modelAndView.addObject("stage", 2);
//			modelAndView.addObject("successMessage", "One Time Password successfully sent.");
//			return modelAndView;
//	
//	
//		}
//}
//
