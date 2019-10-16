package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class AnswerDao {


    @PersistenceContext
    private EntityManager entityManager;

    public AnswerEntity createAnswer(AnswerEntity answerEntity) {
        System.out.println("UserDao..." + answerEntity);
        entityManager.persist(answerEntity);
        try {
            return answerEntity;
        } catch (Exception e) {
            return null;
        }
    }

    public AnswerEntity getUserByEmail(final String email) {
        try {
            return entityManager.createNamedQuery("userByEmail", AnswerEntity.class).setParameter("email", email).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }


}