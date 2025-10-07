package ngo.nabarun.web.api.controller;
import lombok.RequiredArgsConstructor;
import ngo.nabarun.web.api.dto.SuccessResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/requests")
@RequiredArgsConstructor
public class RequestController {
    //private final RequestService requestService;

    @PostMapping("/create")
    public ResponseEntity<SuccessResponse<String>> createOnboarding() {
        return new SuccessResponse<String>().payload("Hello").get(HttpStatus.OK);
    }
}