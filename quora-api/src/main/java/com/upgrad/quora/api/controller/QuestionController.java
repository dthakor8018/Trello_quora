package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.QuestionRequest;
import com.upgrad.quora.api.model.QuestionResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AuthenticationService authenticationService;


    @RequestMapping(method = RequestMethod.POST, path = "/question/create",  consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(final QuestionRequest questionRequest, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UnsupportedEncodingException {

        String accessToken = authorization.split("Bearer ")[1];
        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(accessToken);

        if ( userAuthTokenEntity.getLogoutAt() != null || ZonedDateTime.now().isAfter(userAuthTokenEntity.getExpiresAt()) ) {
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post a question");
        }

        final QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setDate(ZonedDateTime.now());
        questionEntity.setUser(userAuthTokenEntity.getUser());

        final QuestionEntity createQuestionEntity = questionService.upload(questionEntity);
        QuestionResponse questionResponse = new QuestionResponse().id(createQuestionEntity.getUuid()).status("Question SUCCESSFULLY REGISTERED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }
}