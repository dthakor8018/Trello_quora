package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.service.business.AdminBusinessService;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.CommonService;
import com.upgrad.quora.service.business.PasswordCryptographyProvider;
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
public class AdminController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    @Autowired
    private CommonService commonService;

    @Autowired
    private AdminBusinessService adminBusinessService;

    //AdminController error code and  messages
    static String[] errorCodeList = {"ATHR-001", "ATHR-002", "ATHR-003", "USR-001"};
    static String[] errorCodeMessage = {
            "User has not signed in",                             // 0 - ATHR-001
            "User is signed out",                                 // 1 - ATHR-002
            "Unauthorized Access, Entered user is not an admin",  // 2 - ATHR-003
            "User with entered uuid to be deleted does not exist" // 3 - USR-001
        };

    /*
     * This endpoint is used to delete a user in the Quora Application.
     * input - userId and authorization string containing auth token of admin user only
     *
     * output - Success - UserDeleteResponse containing deleted user uuid
     *          Failure - Failure Code  with message.
     */
    @RequestMapping(
            method = RequestMethod.DELETE,
            path = "/admin/user/{userId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDeleteResponse> userDelete(
            @PathVariable("userId") final String userId,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, UserNotFoundException{

        // Call authenticationService with access token came in authorization field.
        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);

        // Token exist but user logged out already or token expired
        if ( userAuthTokenEntity.getLogoutAt() != null ) {
            throw new AuthorizationFailedException(errorCodeList[1],errorCodeMessage[1]);
        }

        // Token belong to a non user
        if ( userAuthTokenEntity.getUser().getRole().compareTo("admin") != 0 ) {
            throw new AuthorizationFailedException(errorCodeList[2],errorCodeMessage[2]);
        }

        //get userEntity which need to be deleted
        UserEntity userEntity = commonService.getUserByUuid(userId);

        //requested userEntity which need to be deleted, doesn't exist
        if( userEntity == null ) {
            throw new UserNotFoundException(errorCodeList[3],errorCodeMessage[3]);
        }

        //Call adminBusinessService to delete user Entity
        adminBusinessService.userDelete(userEntity);

        //create userDeleteResponse with deleted user uuid
        UserDeleteResponse userDeleteResponse = new UserDeleteResponse();
        userDeleteResponse.setId(userEntity.getUuid());
        userDeleteResponse.setStatus("USER SUCCESSFULLY DELETED");
        return new ResponseEntity<UserDeleteResponse>(userDeleteResponse, HttpStatus.OK);

    }
}
