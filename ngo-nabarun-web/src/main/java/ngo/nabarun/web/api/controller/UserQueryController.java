package ngo.nabarun.web.api.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ngo.nabarun.application.dto.command.UserCommand.UserSelfUpdateCommand;
import ngo.nabarun.application.dto.command.UserCommand.UserUpdateCommand;
import ngo.nabarun.application.dto.result.UserResult;
import ngo.nabarun.application.service.UserService;
import ngo.nabarun.application.service.UserService.PagedResult;
import ngo.nabarun.domain.user.enums.UserStatus;
import ngo.nabarun.web.api.dto.SuccessResponse;

@RestController
@RequestMapping("/api/v2/user")
public class UserQueryController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public ResponseEntity<SuccessResponse<PagedResult<UserResult>>> list(
            @RequestParam(name = "emailLike", required = false) String emailLike,
            @RequestParam(name = "status", required = false) UserStatus status,
            @RequestParam(name = "roleCode", required = false) String roleCode,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", required = false) String sort
    ) {
        PagedResult<UserResult> result = userService.listUsers(emailLike, status, roleCode, page, size, sort);
        return new SuccessResponse<PagedResult<UserResult>>().payload(result).get(HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserResult>> get(@PathVariable String id) {
        Optional<UserResult> res = userService.getUser(id);
        return res.map(r -> new SuccessResponse<UserResult>().payload(r).get(HttpStatus.OK))
                  .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/update-my-details")
    public ResponseEntity<SuccessResponse<UserResult>> updateMyDetails(@PathVariable String id, @RequestBody UserSelfUpdateCommand body) {
        UserResult res = userService.updateMyDetails(id, body);
        return new SuccessResponse<UserResult>().payload(res).get(HttpStatus.OK);
    }

    @PatchMapping("/{id}/update-user")
    public ResponseEntity<SuccessResponse<UserResult>> updateUser(@PathVariable String id, @RequestBody UserUpdateCommand body) {
        UserResult res = userService.updateUser(id, body);
        return new SuccessResponse<UserResult>().payload(res).get(HttpStatus.OK);
    }
}