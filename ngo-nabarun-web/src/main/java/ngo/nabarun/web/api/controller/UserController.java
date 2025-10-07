package ngo.nabarun.web.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ngo.nabarun.application.dto.command.UserCommand.UserCreateCommand;
import ngo.nabarun.application.dto.result.UserResult;
import ngo.nabarun.application.service.UserService;
import ngo.nabarun.web.api.dto.SuccessResponse;

@RestController
@RequestMapping("/api/v2/users")
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/create")
	public ResponseEntity<SuccessResponse<UserResult>> createUser(@RequestBody UserCreateCommand user) {
		return new SuccessResponse<UserResult>().payload(userService.createUser(user)).get(HttpStatus.OK);
	}
}
