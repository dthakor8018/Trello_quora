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

    /*
    * This endpoint is used to register a new user in the Quora Application.
    * input - signupUserRequest contain all user details like
    *  First Name, Last, User Name, Email id, about me, password, country, contact No.
    *  output - Success - SignupUserResponse containing created user detail with its uuid
    *           Failure - Failure Code  with message.
    */
    @RequestMapping(
            method = RequestMethod.POST,
            path = "/user/signup",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> userSignup(
            final SignupUserRequest signupUserRequest)
            throws SignUpRestrictedException {

        //create a new UserEntity Object
        UserEntity userEntity = new UserEntity();

        //create a new random unique uuid and set it to new User Entity
        userEntity.setUuid(UUID.randomUUID().toString());

        //Set All the field of new object from the Request
        userEntity.setFirstName(signupUserRequest.getFirstName());
        userEntity.setLastName(signupUserRequest.getLastName());
        userEntity.setUserName(signupUserRequest.getUserName());
        userEntity.setAboutMe(signupUserRequest.getAboutMe());
        userEntity.setDob(signupUserRequest.getDob());
        userEntity.setEmail(signupUserRequest.getEmailAddress());
        userEntity.setPassword(signupUserRequest.getPassword());
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setContactNumber(signupUserRequest.getContactNumber());

        // set Role to default nonadmin
        userEntity.setRole("nonadmin");

        //Call signupBusinessService to create a new user Entity
        final UserEntity createdUserEntity = signupBusinessService.signup(userEntity);

        //create response with create user uuid
        SignupUserResponse userResponse = new SignupUserResponse().id(createdUserEntity.getUuid()).status("USER SUCCESSFULLY REGISTERED");

        return new ResponseEntity<SignupUserResponse>(userResponse, HttpStatus.CREATED);

    }

    /*
     * This endpoint is used to sign in or login user in the Quora Application.
     * input - authorization field containing Basic + Base64 Encoded String of "user name:password"
     *  output - Success - Auth Token with user uuid
     *           Failure - Failure Code  with message.
     */
    @RequestMapping(
            method = RequestMethod.POST,
            path = "/user/signin",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signin(
            @RequestHeader("authorization") final String authorization)
            throws AuthenticationFailedException {

        //split and extract authorization base 64 code string from "authorization" field
        String[] base64EncodedString = authorization.split("Basic ");

        //decode base64 string from a "authorization" field
        byte[] decodedArray = passwordCryptographyProvider.getBase64DecodedStringAsBytes(base64EncodedString[1]);

        String decodedString = new String(decodedArray);

        //decoded string contain user name and password separated by ":"
        String[] decodedUserNamePassword = decodedString.split(":");

        //call authenticationService service to generate user Auth Token for any further communication
        UserAuthTokenEntity userAuthToken = authenticationService.authenticateByUserNamePassword(decodedUserNamePassword[0], decodedUserNamePassword[1]);

        //get userEntity from Auth Token
        UserEntity user = userAuthToken.getUser();

        //send response with user uuid and access token in HttpHeader
        SigninResponse signinResponse = new SigninResponse().id(user.getUuid()).message("SIGNED IN SUCCESSFULLY");
        HttpHeaders headers = new HttpHeaders();
        headers.add("access_token", userAuthToken.getAccessToken());

        return new ResponseEntity<SigninResponse>(signinResponse, headers, HttpStatus.OK);
    }

    /*
     * This endpoint is used to signout user from the Quora Application.
     * input - authorization field containing auth token generated from user sign-in
     *  output - Success - with user uuid
     *           Failure - Failure Code  with message.
     */
    @RequestMapping(
            method = RequestMethod.POST,
            path = "/user/signout",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signout(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, SignOutRestrictedException {

        UserAuthTokenEntity userAuthTokenEntity = null;
        try {
            // Call authenticationService with access token came in authorization field.
            userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);
        } catch(Exception e){
            throw new AuthorizationFailedException(errorCodeList[4],errorCodeMessage[4]);
        }

        // Token exist but user logged out already or token expired
        if ( userAuthTokenEntity.getLogoutAt() != null ) {
            throw new AuthorizationFailedException(errorCodeList[4],errorCodeMessage[4]);
        }

        //Set logout time
        userAuthTokenEntity.setLogoutAt(ZonedDateTime.now());

        //update userAuthTokenEntity with updated logout time.
        authenticationService.updateUserAuthToken(userAuthTokenEntity);

        //create response with signed out user uuid
        SignoutResponse signoutResponse = new SignoutResponse().id(userAuthTokenEntity.getUser().getUuid()).message("SIGNED OUT SUCCESSFULLY");
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<SignoutResponse>(signoutResponse, headers, HttpStatus.OK);
    }
}
