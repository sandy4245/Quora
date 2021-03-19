package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping ("/")
public class AnswerController {

    @Autowired
    private AnswerService answerService;
    //This endpoint is used to create an answer to a particular question. Any user can access this endpoint.
    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(@PathVariable("questionId") final String questionId, final AnswerRequest answerRequest, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        final AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setDate(ZonedDateTime.now());
        answerEntity.setAnswer(answerRequest.getAnswer());


        final AnswerEntity createdAnswerEntity = answerService.createAnswer(answerEntity, authorization, questionId);
        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswerEntity.getUuid()).status("Answer Created");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }
    //This endpoint is used to edit an answer. Only the owner of the answer can edit the answer.
    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> editAnswer(final AnswerEditRequest answerEditRequest, @PathVariable("answerId") final String answerId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        final AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setAnswer(answerEditRequest.getContent());
        answerEntity.setUuid(answerId);
        answerEntity.setDate(ZonedDateTime.now());


        final AnswerEntity updatedAnswerEntity = answerService.editAnswer(answerEntity, authorization, answerId);
        AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(updatedAnswerEntity.getUuid()).status("Answer Edited");
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }
    //This endpoint is used to delete an answer. Only the owner of the answer or admin can delete an answer.
    @RequestMapping(path = "/answer/delete/{answerId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnswer(@PathVariable("answerId") final String answerId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        //Delete requested answer from db
        answerService.deleteAnswer(answerId, authorization);

        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse().id(answerId).status("ANSWER DELETED");

        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse, HttpStatus.OK);
    }
    //This endpoint is used to get all answers to a particular question. Any user can access this endpoint.
    @RequestMapping(path = "answer/all/{questionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswersToQuestion(@PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        QuestionEntity questionEntity = answerService.getQuestion(questionId);
        final List<AnswerEntity> allAnswers = answerService.getAllAnswers(questionId, authorization);
        List<AnswerDetailsResponse> answerDetailsResponseList = new ArrayList<>();
        for (AnswerEntity ae : allAnswers) {
            answerDetailsResponseList.add(new AnswerDetailsResponse().id(ae.getUuid()).answerContent(ae.getAnswer()).questionContent(questionEntity.getContent()));
        }
        return new ResponseEntity<List<AnswerDetailsResponse>>(answerDetailsResponseList, HttpStatus.OK);

    }
}
