package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.PasswordCryptographyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class AdminController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    @RequestMapping(method = RequestMethod.GET, path = "/admin/user/{userId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDeleteResponse> userDelete(@PathVariable("userId") final String userId, @RequestHeader("authorization") final String authorization) {

        UserDeleteResponse userDeleteResponse = new UserDeleteResponse();
        return new ResponseEntity<UserDeleteResponse>(userDeleteResponse, HttpStatus.OK);

    }
}
