package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.AnswerRequest;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.CommonService;
import com.upgrad.quora.service.business.PasswordCryptographyProvider;
import com.upgrad.quora.service.business.SignupBusinessService;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import io.swagger.annotations.ApiImplicitParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class CommonController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private CommonService commonService;

    //CommonController error code and  messages
    static String[] errorCodeList = {"ATHR-001", "ATHR-002", "USR-001"};
    static String[] errorCodeMessage = {
            "User has not signed in",                               // 0 - ATHR-001
            "User is signed out.Sign in first to get user details", // 1 - ATHR-002
            "User with entered uuid does not exist"                 // 2 - USR-001
        };
    @RequestMapping(
            method = RequestMethod.GET,
            path = "/userprofile/{userId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDetailsResponse> getUserDetail(
            @PathVariable("userId") final String userId,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, UserNotFoundException{

        UserEntity userEntity = commonService.getUserByUuid(userId);

        if( userEntity == null ) {
            throw new UserNotFoundException(errorCodeList[2],errorCodeMessage[2]);
        }

        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);

        if ( userAuthTokenEntity.getLogoutAt() != null || ZonedDateTime.now().isBefore(userAuthTokenEntity.getExpiresAt())) {
            throw new AuthorizationFailedException(errorCodeList[1],errorCodeMessage[1]);
        }


        UserDetailsResponse userDetailsResponse = new UserDetailsResponse();
        userDetailsResponse.setUserName(userEntity.getUserName());
        userDetailsResponse.setAboutMe(userEntity.getAboutMe());
        userDetailsResponse.setContactNumber(userEntity.getContactNumber());
        userDetailsResponse.setCountry(userEntity.getCountry());
        userDetailsResponse.setDob(userEntity.getDob());
        userDetailsResponse.setEmailAddress(userEntity.getEmail());
        userDetailsResponse.setFirstName(userEntity.getFirstName());
        userDetailsResponse.setLastName(userEntity.getLastName());

        return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);

    }
}
