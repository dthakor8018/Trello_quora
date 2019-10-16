package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class QuestionDao {


    @PersistenceContext
    private EntityManager entityManager;

    public QuestionEntity createQuestion(QuestionEntity questionEntity) {
        System.out.println("UserDao..." + questionEntity);
        entityManager.persist(questionEntity);
        try {
            return questionEntity;
        } catch (Exception e) {
            return null;
        }
    }

    public QuestionEntity getUserByEmail(final String email) {
        try {
            return entityManager.createNamedQuery("userByEmail", QuestionEntity.class).setParameter("email", email).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }


}