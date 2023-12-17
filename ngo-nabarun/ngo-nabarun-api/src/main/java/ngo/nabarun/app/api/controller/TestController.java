package ngo.nabarun.app.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import ngo.nabarun.app.api.response.SuccessResponse;
import ngo.nabarun.app.businesslogic.IUserBL;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.helper.DTOToBusinessObjectConverter;
import ngo.nabarun.app.common.enums.AddressType;
import ngo.nabarun.app.common.enums.PhoneType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.ext.objects.RemoteConfig;
import ngo.nabarun.app.ext.service.IRemoteConfigExtService;
import ngo.nabarun.app.infra.core.entity.UserProfileEntity;
import ngo.nabarun.app.infra.core.repo.UserProfileRepository;
import ngo.nabarun.app.infra.dto.AddressDTO;
import ngo.nabarun.app.infra.dto.PhoneDTO;
import ngo.nabarun.app.infra.dto.UserAdditionalDetailsDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.misc.UserConfigTemplate;
import ngo.nabarun.app.infra.service.IDomainRefConfigInfraService;
import ngo.nabarun.app.infra.service.IUserInfraService;

@RestController
@SecurityRequirement(name = "nabarun_auth")
public class TestController {

	@Autowired
	private IUserBL userService;

	@Autowired
	private IRemoteConfigExtService serv;

	@Autowired
	private IUserInfraService userInfraService;

	@Autowired
	private UserProfileRepository upRepo;

	@PostMapping("/api/test/createuser")
	public UserDetail getUserEmail(@RequestParam String firstname, @RequestParam String lastname,
			@RequestParam String email, @RequestParam String phone, @RequestParam String hometown) throws Exception {
		UserDTO userDTO = new UserDTO();
		userDTO.setFirstName(firstname);
		userDTO.setLastName(lastname);
		userDTO.setEmail(email);
		userDTO.setTitle("MR");
		userDTO.setGender("M");// derive gender from title
		UserAdditionalDetailsDTO uadDTO = new UserAdditionalDetailsDTO();
		uadDTO.setDisplayPublic(false);
		uadDTO.setActiveContributor(true);
		userDTO.setAdditionalDetails(uadDTO);
		PhoneDTO cnt = new PhoneDTO();
		cnt.setPhoneCode("+91");
		cnt.setPhoneNumber(phone);
		cnt.setPhoneType(PhoneType.MOBILE);

		userDTO.setPhones(List.of(cnt));
		userDTO.setStatus(ProfileStatus.ACTIVE);
		AddressDTO add = new AddressDTO();
		add.setAddressType(AddressType.PERMANENT);
		add.setHometown(hometown);
		userDTO.setAddresses(List.of(add));
		return DTOToBusinessObjectConverter.toUserDetail(userInfraService.createUser(userDTO));
	}

	@GetMapping("/getconfig")
	public ResponseEntity<SuccessResponse<List<RemoteConfig>>> getconfig() throws Exception {
		return new SuccessResponse<List<RemoteConfig>>().payload(serv.getRemoteConfigs()).get(HttpStatus.OK);
	}
	
	@Autowired 
	private IDomainRefConfigInfraService domainRefConfig;
	@GetMapping(value = "/testing",produces="application/json")
	public ResponseEntity<SuccessResponse<Object>> testing(@RequestParam String email)
			throws Exception {
//		ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase()
//				.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
//		UserProfileEntity e = new UserProfileEntity();
//		e.setEmail(email);
//		Example<UserProfileEntity> example = Example.of(e);
//		Pageable page = Pageable.ofSize(10);
//
//		List<UserProfileEntity> result = upRepo.findAll(example);
//		System.err.println(result);
		UserConfigTemplate userConfig=domainRefConfig.getUserConfig();
		System.out.println(userConfig);
		System.out.println(domainRefConfig.getDonationConfig());
		return new SuccessResponse<Object>().payload(userConfig).get(HttpStatus.OK);
	}
}
