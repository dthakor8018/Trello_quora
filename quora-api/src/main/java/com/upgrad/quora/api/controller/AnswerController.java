package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.AnswerDetailsResponse;
import com.upgrad.quora.api.model.AnswerRequest;
import com.upgrad.quora.api.model.AnswerResponse;
import com.upgrad.quora.service.business.AnswerService;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import java.util.List;
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

  @Autowired private AnswerService answerService;

  @Autowired private AuthenticationService authenticationService;

  @Autowired private QuestionService questionService;

  @RequestMapping(
      method = RequestMethod.POST,
      path = "/question/{questionId}/answer/create",
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<AnswerResponse> addAnswer(
      final AnswerRequest answerRequest,
      @RequestParam("questionId") final String questionUuid,
      @RequestHeader("authorization") final String authorization)
      throws AuthorizationFailedException, UnsupportedEncodingException {

    String accessToken = authorization.split("Bearer ")[1];
    UserAuthTokenEntity userAuthTokenEntity =
        authenticationService.authenticateByAccessToken(accessToken);
    if (userAuthTokenEntity == null) {
      throw new AuthorizationFailedException(
          "UP-001", "User is not Signed in, sign in to upload Question");
    }

    final QuestionEntity questionEntity = questionService.getQuestionByUuid(questionUuid);

    final AnswerEntity answerEntity = new AnswerEntity();
    answerEntity.setAns(answerRequest.getAnswer());
    answerEntity.setUuid(UUID.randomUUID().toString());
    answerEntity.setDate(ZonedDateTime.now());
    answerEntity.setUser(userAuthTokenEntity.getUser());
    answerEntity.setQuestion(questionEntity);

    final AnswerEntity createAnswerEntity = answerService.createAnswer(answerEntity);
    AnswerResponse questionResponse =
        new AnswerResponse()
            .id(createAnswerEntity.getUuid())
            .status("Answer SUCCESSFULLY REGISTERED");
    return new ResponseEntity<AnswerResponse>(questionResponse, HttpStatus.CREATED);
  }

  @RequestMapping(
      method = RequestMethod.GET,
      path = "answer/all/{questionId}",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<List<AnswerEntity>> getAllAnswers(
      @PathVariable("questionId") final String imageUuid,
      @RequestHeader("authorization") final String authorization)
      throws AuthorizationFailedException, InvalidQuestionException {

    final List<AnswerEntity> answerEntity = answerService.getAllAnswer(imageUuid, authorization);
    return ResponseEntity.status(HttpStatus.OK).body(answerEntity);
  }

  @RequestMapping(
      method = RequestMethod.PUT,
      path = "/answer/edit/{answerId}",
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<AnswerResponse> editAnswerContent(
      final String content,
      @PathVariable("answerId") final long answerId,
      @RequestHeader("authorization") final String authorization)
      throws AuthorizationFailedException, AnswerNotFoundException {
    AnswerEntity answerEntity = new AnswerEntity();

    answerEntity.setAns(content);
    answerEntity.setId(answerId);
    AnswerEntity updatedAnswerEntity = answerService.updateAnswer(answerEntity, authorization);
    AnswerResponse answerDetailsResponse =
        new AnswerResponse().id(updatedAnswerEntity.getUuid()).status("ANSWER EDITED");

    return new ResponseEntity<AnswerResponse>(answerDetailsResponse, HttpStatus.OK);
  }

  @RequestMapping(
      method = RequestMethod.DELETE,
      path = "/answer/delete/{answerId}",
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity deleteAnswer(
      @PathVariable("answerId") final long answerId,
      @RequestHeader("authorization") final String authorization)
      throws AuthorizationFailedException, AnswerNotFoundException {
    AnswerEntity answerEntity = new AnswerEntity();
    answerEntity.setId(answerId);
    answerService.deleteAnswer(answerEntity, authorization);
    AnswerResponse answerDetailsResponse =
        new AnswerResponse().id(answerEntity.getUuid()).status("ANSWER DELETED");

    return ResponseEntity.status(HttpStatus.OK).body(answerDetailsResponse);
  }
}
