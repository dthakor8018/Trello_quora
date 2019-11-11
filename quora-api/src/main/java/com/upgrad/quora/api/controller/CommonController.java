package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.CommonService;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

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

    /*
     * This endpoint is used to get detail of a user in the Quora Application.
     * input - userUuid and authorization string containing auth token of current user
     *
     * output - Success - UserDetailsResponse containing details of user for given uuid
     *          Failure - Failure Code  with message.
     */
    @RequestMapping(
            method = RequestMethod.GET,
            path = "/userprofile/{userId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDetailsResponse> getUserDetail(
            @PathVariable("userId") final String userUuid,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, UserNotFoundException{

        //Call commonService to get user Entity for given userUuid
        UserEntity userEntity = commonService.getUserByUuid(userUuid);

        //user dosen't exist for given uuid
        if( userEntity == null ) {
            throw new UserNotFoundException(errorCodeList[2],errorCodeMessage[2]);
        }

        // Call authenticationService with access token came in authorization field.
        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);

        // Token exist but user logged out already or token expired
        if ( userAuthTokenEntity.getLogoutAt() != null ) {
            throw new AuthorizationFailedException(errorCodeList[1],errorCodeMessage[1]);
        }

        //create UserDetailsResponse from the data of user Entity
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
