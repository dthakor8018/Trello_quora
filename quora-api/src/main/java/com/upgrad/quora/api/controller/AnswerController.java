package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.AnswerRequest;
import com.upgrad.quora.api.model.AnswerResponse;
import com.upgrad.quora.service.business.AnswerService;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private QuestionService questionService;

    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> addAnswer(final AnswerRequest answerRequest, @RequestParam("questionId") final String questionUuid, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UnsupportedEncodingException {

        String accessToken = authorization.split("Bearer ")[1];
        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(accessToken);

        if ( userAuthTokenEntity.getLogoutAt() != null || ZonedDateTime.now().isAfter(userAuthTokenEntity.getExpiresAt()) ) {
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post an answer");
        }

        final QuestionEntity questionEntity = questionService.getQuestionByUuid(questionUuid);

        final AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setAns(answerRequest.getAnswer());
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setDate(ZonedDateTime.now());
        answerEntity.setUser(userAuthTokenEntity.getUser());
        answerEntity.setQuestion(questionEntity);

        final AnswerEntity createAnswerEntity = answerService.createAnswer(answerEntity);
        AnswerResponse questionResponse = new AnswerResponse().id(createAnswerEntity.getUuid()).status("Answer SUCCESSFULLY REGISTERED");
        return new ResponseEntity<AnswerResponse>(questionResponse, HttpStatus.CREATED);
    }
}