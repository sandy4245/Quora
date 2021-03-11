package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class CommonController {

    @Autowired
    private UserBusinessService userBusinessService;

    @GetMapping(path = "/userprofile/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDetailsResponse> userDetailsResponse(@PathVariable("userId") final String userUuid, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UserNotFoundException {

        UserEntity userDetails = userBusinessService.getUserDetails(userUuid, authorization);

        UserDetailsResponse userDetailsResponse = new UserDetailsResponse()
                .firstName(userDetails.getFirstName())
                .lastName(userDetails.getLastName())
                .emailAddress(userDetails.getEmail())
                .userName(userDetails.getUserName())
                .aboutMe(userDetails.getAboutMe())
                .country(userDetails.getCountry())
                .contactNumber(userDetails.getContactNumber())
                .dob(String.valueOf(userDetails.getDob()));

        return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);
    }
}
