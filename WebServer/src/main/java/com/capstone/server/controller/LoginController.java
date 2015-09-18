
package com.capstone.server.controller;

import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.capstone.server.dao.TeenDao;
import com.capstone.server.dao.UserDao;
import com.capstone.server.model.JsonResponse;
import com.capstone.server.model.Teen;
import com.capstone.server.model.User;
import com.capstone.server.utils.Constants;
import com.capstone.server.utils.Constants.SignInProvider;

@Controller
@RequestMapping(RestUriConstants.LOGIN_CONTROLLER)
public class LoginController implements MessageSourceAware {

    private final static Logger sLogger = Logger.getLogger(LoginController.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private TeenDao teenDao;

    private MessageSource messageSource;

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @RequestMapping(value = RestUriConstants.SEND, method = RequestMethod.POST)
    public @ResponseBody JsonResponse sendLogin(@RequestBody final User user, HttpSession session, Locale locale) {

        User databaseUser = userDao.find(user.getEmail());

        if (databaseUser != null) {
            // TODO - find a more elegant way of doing the password verification
            if (user.getProvider().equals(SignInProvider.APPLICATION)
                    && databaseUser.getPassword().equals(user.getPassword())) {
                session.setAttribute(Constants.SESSION_USER, user);
            } else if (user.getProvider().equals(SignInProvider.FACEBOOK)
                    && databaseUser.getFacebookId().equals(user.getFacebookId())) {
                session.setAttribute(Constants.SESSION_USER, user);
            }
        }

        String output;

        // if user session has been set, then the login succeeded
        if (session.getAttribute(Constants.SESSION_USER) != null) {
            output = messageSource.getMessage("label.loginController.loginSuccess",
                    new Object[] { user.getEmail() }, locale);
            sLogger.info(output);

            return new JsonResponse(HttpStatus.OK, output);
        } else {
            output = messageSource.getMessage("label.loginController.loginFail",
                    new Object[] { user.getEmail() }, locale);
            sLogger.info(output);

            return new JsonResponse(HttpStatus.BAD_REQUEST, output);
        }
    }

    @RequestMapping(RestUriConstants.SUBMIT)
    public String submitLogin() {
        return "redirect:/";
    }

    @RequestMapping(RestUriConstants.REGISTER)
    public String showRegisterForm() {
        return "login/register";
    }

    @RequestMapping(RestUriConstants.REGISTER + "/" + RestUriConstants.SUBMIT)
    public @ResponseBody JsonResponse registerFormSubmit(@RequestBody final User user, Locale locale) {

        User databaseUser = userDao.find(user.getEmail());

        String output = null;
        if (databaseUser == null) {
            // sets the User of the Teen since I get "Converting
            // circular structure to JSON" exception in jQuery when
            // I try to set the User for the Teen and vice-versa.
            Teen teen = user.getTeen();
            teen.setUser(user);

            userDao.persist(user);

            output = "success!!";
            sLogger.info(output);

            return new JsonResponse(HttpStatus.OK, output);
        } else {
            output = "already exists!!";
            sLogger.info(output);

            return new JsonResponse(HttpStatus.BAD_REQUEST, output);
        }
    }

    @RequestMapping(RestUriConstants.LOGOUT)
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
