package com.upgrad.quora.service.business;


import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBusinessService {

    @Autowired
    private UserDao userDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteUser(final String userUuid, final String authorization) throws AuthorizationFailedException, UserNotFoundException {

        UserAuthTokenEntity userAuthToken = userDao.getUserAuthToken(authorization);

        //Validate if user is signed in or not
        if(userAuthToken == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        //Validate if user has signed out
        if(userAuthToken.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get user details");
        }

        //Validate user is admin or not
        String role = userAuthToken.getUser().getRole();

        if(role.equals("nonadmin")){
            throw new AuthorizationFailedException("ATHR-003","Unauthorized Access, Entered user is not an admin");
        }

        //Validate if user exists or not
        UserEntity userEntity = userDao.getUser(userUuid);
        if(userEntity == null){
            throw new UserNotFoundException("USR-001","User with entered uuid does not exist");
        }

        userDao.deleteUser(userUuid);
    }
}
