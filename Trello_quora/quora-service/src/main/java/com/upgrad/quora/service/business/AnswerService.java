package com.upgrad.quora.service.business;


import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnswerService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private AnswerDao answerDao;

    @Transactional (propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(AnswerEntity answerEntity, final String authorizationToken, final String questionId) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        //Validate if user is signed in or not
        if(userAuthTokenEntity == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        //Validate if user has signed out
        if(userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post an answer");
        }

        QuestionEntity questionEntity = answerDao.getQuestion(questionId);

        //Validate if question is valid
        if (questionEntity == null){
            throw new InvalidQuestionException("QUES-001","The question entered is invalid");
        }
        answerEntity.setUser(userAuthTokenEntity.getUser());
        answerEntity.setQuestion(questionEntity);
        return answerDao.createAnswer(answerEntity);
    }

    @Transactional (propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswer(AnswerEntity answerEntity, final String authorizationToken, final String answerId) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        //Validate if user is signed in or not
        if(userAuthTokenEntity == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        //Validate if user has signed out
        if(userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to edit an answer");
        }

        // If the answer with uuid which is to be edited does not exist in the database,
        // throw "AnswerNotFoundException" with the message code - 'ANS-001' and message - 'Entered answer uuid does not exist'.
        if (answerDao.getAnswer(answerId) == null){
            throw new AnswerNotFoundException("ANS-001","Entered answer uuid does not exist");
        }

        // Only the answer owner can edit the answer. Therefore, if the user who is not the owner of the answer tries to edit the answer
        // throw "AuthorizationFailedException" with the message code - 'ATHR-003' and message - 'Only the answer owner can edit the answer'.
        UserEntity answerOwner = answerDao.getAnswer(answerId).getUser();
        QuestionEntity questionEntity = answerDao.getAnswer(answerId).getQuestion();
        if (answerOwner.getUuid().equals(userAuthTokenEntity.getUuid())){
            answerEntity.setUser(userAuthTokenEntity.getUser());
            answerEntity.setQuestion(questionEntity);
            return answerDao.editAnswer(answerEntity);
        } else {
            throw new AuthorizationFailedException ("ATHR-003","Only the answer owner can edit the answer");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteAnswer(final String answerId, final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthTokenEntity userAuthToken = userDao.getUserAuthToken(authorization);

        //Validate if user is signed in or not
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        //Validate if user has signed out
        if (userAuthToken.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete an answer");
        }
        // Validate if the answer with uuid which is to be deleted does exist in the database,
        if (answerDao.getAnswer(answerId) == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }

        //Validate user is admin or owner of the answer or else throw an exception
        String role = userAuthToken.getUser().getRole();
        UserEntity answerOwner = answerDao.getAnswer(answerId).getUser();

        if (answerOwner.getUuid().equals(userAuthToken.getUuid()) || role.equals("admin")) {
            answerDao.deleteAnswer(answerId);
        } else {
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
        }


    }

    public QuestionEntity getQuestion (final String questionId) {
        return answerDao.getQuestion(questionId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<AnswerEntity> getAllAnswers(final String questionId, final String authorization) throws InvalidQuestionException, AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorization);
        //Validate if user is signed in or not
        if(userAuthTokenEntity == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        //Validate if user has signed out
        if(userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get the answers");
        }
        QuestionEntity questionEntity = answerDao.getQuestion(questionId);
        //Validate if question is valid
        if (questionEntity == null){
            throw new InvalidQuestionException("QUES-001","The question with entered uuid whose details are to be seen does not exist");
        }
        //If user is authorised and question is valid, return all answers along with answer id and question content
        List<AnswerEntity> allAnswers = answerDao.getAllAnswers(questionId);
        return allAnswers;

    }
}
