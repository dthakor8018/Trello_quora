package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.UserAuthDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnswerService {

  @Autowired private AnswerDao answerDao;

  @Autowired private UserAuthDao userAuthDao;

  @Transactional(propagation = Propagation.REQUIRED)
  public AnswerEntity createAnswer(AnswerEntity answerEntity) throws AuthorizationFailedException {
    return answerDao.createAnswer(answerEntity);
  }

  public List<AnswerEntity> getAllAnswer(final String questionUUid, final String accessToken)
      throws AuthorizationFailedException, InvalidQuestionException {
    UserAuthTokenEntity userAuthTokenEntity = null;
    try {
      userAuthTokenEntity = userAuthDao.getUserAuthTokenEntityByAccessToken(accessToken);
    } catch (Exception e) {
      throw new AuthorizationFailedException("ATHR-001", "User has not signed in.");
    }

    if (userAuthTokenEntity == null) {
      throw new AuthorizationFailedException(
          "ATHR-002", "User is signed out.Sign in first to get the answers.");
    }

    String role = userAuthTokenEntity.getUser().getRole();
    if (role.equals("admin")) {
      List<AnswerEntity> answerEntity = answerDao.getAllAnswers(questionUUid);
      return answerEntity;
    } else
      throw new InvalidQuestionException(
          "QUES-001", "The question with entered uuid whose details are to be seen does not exist.");
  }
}
