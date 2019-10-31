package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SigninResponse;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.PasswordCryptographyProvider;
import com.upgrad.quora.service.business.SignupBusinessService;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private SignupBusinessService signupBusinessService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    //AdminController error code and  messages
    static String[] errorCodeList = {"SGR-001", "SGR-002", "ATH-001", "ATH-002", "SGR-001"};

    static String[] errorCodeMessage = {
            "Try any other Username, this Username has already been taken",		 // 0 - SGR-001
            "This user has already been registered, try with any other emailId", // 1 - SGR-002
            "This username does not exist",										 // 2 - ATH-001
            "Password failed",													 // 3 - ATH-002
            "User is not Signed in"												 // 4 - SGR-001
        };

    @RequestMapping(
            method = RequestMethod.POST,
            path = "/user/signup",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> userSignup(
            final SignupUserRequest signupUserRequest)
            throws SignUpRestrictedException {

        UserEntity userEntity = new UserEntity();

        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setFirstName(signupUserRequest.getFirstName());
        userEntity.setLastName(signupUserRequest.getLastName());
        userEntity.setUserName(signupUserRequest.getUserName());
        userEntity.setAboutMe(signupUserRequest.getAboutMe());
        userEntity.setDob(signupUserRequest.getDob());
        userEntity.setEmail(signupUserRequest.getEmailAddress());
        userEntity.setPassword(signupUserRequest.getPassword());
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setContactNumber(signupUserRequest.getContactNumber());
        userEntity.setRole("nonadmin");

        final UserEntity createdUserEntity = signupBusinessService.signup(userEntity);
        SignupUserResponse userResponse = new SignupUserResponse().id(createdUserEntity.getUuid()).status("USER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupUserResponse>(userResponse, HttpStatus.CREATED);

    }

    @RequestMapping(
            method = RequestMethod.POST,
            path = "/user/signin",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signin(
            @RequestHeader("authorization") final String authorization)
            throws AuthenticationFailedException {

        String[] base64EncodedString = authorization.split("Basic ");

        byte[] decodedArray = passwordCryptographyProvider.getBase64DecodedStringAsBytes(base64EncodedString[1]);

        String decodedString = new String(decodedArray);

        String[] decodedUserNamePassword = decodedString.split(":");

        UserAuthTokenEntity userAuthToken = authenticationService.authenticateByUserNamePassword(decodedUserNamePassword[0], decodedUserNamePassword[1]);

        UserEntity user = userAuthToken.getUser();

        SigninResponse signinResponse = new SigninResponse().id(user.getUuid()).message("SIGNED IN SUCCESSFULLY");
        HttpHeaders headers = new HttpHeaders();
        headers.add("access_token", userAuthToken.getAccessToken());
        return new ResponseEntity<SigninResponse>(signinResponse, headers, HttpStatus.OK);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            path = "/user/signout",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signout(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, SignOutRestrictedException {

        //String accessToken = authorization.split("Bearer ")[1];
        UserAuthTokenEntity userAuthTokenEntity = null;
        try {
            userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);
        } catch(Exception e){
            throw new AuthorizationFailedException(errorCodeList[4],errorCodeMessage[4]);
        }
        if ( userAuthTokenEntity.getLogoutAt() != null || ZonedDateTime.now().isBefore(userAuthTokenEntity.getExpiresAt()) ) {
            throw new AuthorizationFailedException(errorCodeList[4],errorCodeMessage[4]);
        }

        userAuthTokenEntity.setLogoutAt(ZonedDateTime.now());

        authenticationService.updateUserAuthToken(userAuthTokenEntity);

        SignoutResponse signoutResponse = new SignoutResponse().id(userAuthTokenEntity.getUser().getUuid()).message("SIGNED OUT SUCCESSFULLY");
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<SignoutResponse>(signoutResponse, headers, HttpStatus.OK);
    }
}
