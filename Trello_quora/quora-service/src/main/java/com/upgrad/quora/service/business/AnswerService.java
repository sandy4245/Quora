package com.upgrad.quora.service.business;


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

@Service
public class AnswerService {
    @Autowired
    private UserDao userDao;

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
        QuestionEntity questionEntity = userDao.getQuestion(questionId);

        //Validate if question is valid
        if (questionEntity == null){
            throw new InvalidQuestionException("QUES-001","The question entered is invalid");
        }
        answerEntity.setUser(userAuthTokenEntity.getUser());
        answerEntity.setQuestion(questionEntity);
        return userDao.createAnswer(answerEntity);
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
        if (userDao.getAnswer(answerId) == null){
            throw new AnswerNotFoundException("ANS-001","Entered answer uuid does not exist");
        }

        // Only the answer owner can edit the answer. Therefore, if the user who is not the owner of the answer tries to edit the answer
        // throw "AuthorizationFailedException" with the message code - 'ATHR-003' and message - 'Only the answer owner can edit the answer'.
        UserEntity answerOwner = userDao.getAnswer(answerId).getUser();
        QuestionEntity questionEntity = userDao.getAnswer(answerId).getQuestion();
        if (answerOwner.getUuid().equals(userAuthTokenEntity.getUuid())){
            answerEntity.setUser(userAuthTokenEntity.getUser());
            answerEntity.setQuestion(questionEntity);
            return userDao.editAnswer(answerEntity);
        } else {
            throw new AuthorizationFailedException ("ATHR-003","Only the answer owner can edit the answer");
        }
    }
}
