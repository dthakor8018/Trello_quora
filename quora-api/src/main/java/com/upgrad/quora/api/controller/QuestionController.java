package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.QuestionDetailsResponse;
import com.upgrad.quora.api.model.QuestionRequest;
import com.upgrad.quora.api.model.QuestionResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AuthenticationService authenticationService;


    @RequestMapping(
        method = RequestMethod.POST,
        path = "/question/create",
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(
        final QuestionRequest questionRequest,
        @RequestHeader("authorization") final String authorization)
        throws AuthorizationFailedException, UnsupportedEncodingException {

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
        return new ResponseEntity<>(questionResponse, HttpStatus.CREATED);
    }

    @RequestMapping(
        method = RequestMethod.GET,
        path = "/question/all",
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") final String authorization)
        throws AuthorizationFailedException, UnsupportedEncodingException {

        String accessToken = authorization.split("Bearer ")[1];
        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(accessToken);

        if ( userAuthTokenEntity.getLogoutAt() != null || ZonedDateTime.now().isAfter(userAuthTokenEntity.getExpiresAt()) ) {
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post a question");
        }



        final List<QuestionEntity> questionEntityList = questionService.getAllQuestionsByUser(userAuthTokenEntity.getUser());

        List<QuestionDetailsResponse> questionDetailsResponseList = new ArrayList<>();

        for(QuestionEntity questionEntity: questionEntityList) {

            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse().id(questionEntity.getUuid()).content(questionEntity.getContent());
            questionDetailsResponseList.add(questionDetailsResponse);

        }
        return new ResponseEntity<>(questionDetailsResponseList, HttpStatus.OK);
    }

    @RequestMapping(
            method = RequestMethod.PUT,
            path = "/question/edit/{questionId}",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> editQuestionContent(
            final QuestionRequest questionRequest,
            @PathVariable("questionId") final String questionUuid,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException, UnsupportedEncodingException {

        String accessToken = authorization.split("Bearer ")[1];
        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(accessToken);

        if ( userAuthTokenEntity.getLogoutAt() != null || ZonedDateTime.now().isAfter(userAuthTokenEntity.getExpiresAt()) ) {
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post a question");
        }



        final QuestionEntity questionEntity = questionService.getQuestionByUuid(questionUuid);

        if(questionEntity == null ) {
            throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }

        if(  questionEntity.getUser() != userAuthTokenEntity.getUser() ) {
            //TODO: Chnage ATHR-003 can content in predefined strings
            throw new AuthorizationFailedException("ATHR-003","Only the question owner can edit the question");
        }

        questionEntity.setContent(questionRequest.getContent());

        QuestionEntity updateQuestionEntity = questionService.updateQuestion(questionEntity);

        if( updateQuestionEntity == null ){
            throw new AuthorizationFailedException("OTHER-001","Database Error");
        }

        QuestionResponse questionResponse = new QuestionResponse().id(questionEntity.getUuid()).status("QUESTION EDITED");

        return new ResponseEntity<>(questionResponse, HttpStatus.OK);
    }
}