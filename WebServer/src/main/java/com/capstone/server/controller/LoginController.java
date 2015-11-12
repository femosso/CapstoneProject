
package com.capstone.server.controller;

import static com.capstone.server.utils.Validators.isValidDate;
import static com.capstone.server.utils.Validators.isValidEmail;
import static com.capstone.server.utils.Validators.isValidString;

import java.util.Arrays;
import java.util.HashSet;
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
import com.capstone.server.model.Follower;
import com.capstone.server.model.JsonResponse;
import com.capstone.server.model.LoginResponse;
import com.capstone.server.model.Teen;
import com.capstone.server.model.User;
import com.capstone.server.utils.Constants;
import com.capstone.server.utils.Constants.QuestionType;
import com.capstone.server.utils.Constants.SignInProvider;
import com.capstone.server.utils.Constants.UserType;

@Controller
@RequestMapping(RestUriConstants.LOGIN_CONTROLLER)
public class LoginController implements MessageSourceAware {

    private final static Logger sLogger = Logger.getLogger(LoginController.class);

    private final static boolean DEBUG = sLogger.isDebugEnabled();

    @Autowired
    private UserDao userDao;

    @Autowired
    private TeenDao teenDao;

    private MessageSource messageSource;

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @RequestMapping(value = RestUriConstants.SEND, method = RequestMethod.POST)
    public @ResponseBody LoginResponse sendLogin(@RequestBody final User user,
            HttpSession session, Locale locale) {
        // FIXME - add field validation

        User userDb = userDao.find(user.getEmail());

        if (userDb != null) {
            if (user.getProvider() == SignInProvider.APPLICATION.ordinal()
                    && userDb.getPassword().equals(user.getPassword())) {
                session.setAttribute(Constants.SESSION_USER, userDb);
            } else if (user.getProvider() == SignInProvider.FACEBOOK.ordinal()
                    && userDb.getFacebookId().equals(user.getFacebookId())) {
                session.setAttribute(Constants.SESSION_USER, userDb);
            }
        }

        String output;
        LoginResponse loginResponse = new LoginResponse();

        // if user session has been set, then the login succeeded
        if (session.getAttribute(Constants.SESSION_USER) != null) {
            output = messageSource.getMessage("label.loginController.loginSuccess",
                    new Object[] { user.getEmail() }, locale);
            sLogger.info(output);

            // set relevant information
            user.setType(userDb.getType());

            // if logged user is a teen, load some basic informations (sharedData and etc)
            if (user.getType() == UserType.TEEN.ordinal()) {
                Teen teenDb = teenDao.find(user.getEmail());

                Teen teen = new Teen();
                teen.setSharedData(teenDb.getSharedData());

                user.setTeen(teen);
            }

            loginResponse.setJsonResponse(new JsonResponse(HttpStatus.OK, output));
            loginResponse.setUser(user);
        } else {
            output = messageSource.getMessage("label.loginController.loginFail",
                    new Object[] { user.getEmail() }, locale);
            sLogger.info(output);

            loginResponse.setJsonResponse(new JsonResponse(HttpStatus.BAD_REQUEST, output));
        }

        return loginResponse;
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
        if (!isValidUser(user)) {
            sLogger.info("Invalid parameters");
            return new JsonResponse(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        User databaseUser = userDao.find(user.getEmail());

        String output = null;
        if (databaseUser == null) {
            // sets the User of the Teen since I get "Converting
            // circular structure to JSON" exception in jQuery when
            // I try to set the User for the Teen and vice-versa.
            if (user.getType() == UserType.TEEN.ordinal()) {
                Teen teen = user.getTeen();
                teen.setUser(user);
                teen.setEmail(user.getEmail());

                // at first, all check-in data should be shared
                teen.setSharedDataAsList(new HashSet<>(Arrays.asList(QuestionType.names())));

                // as all teen could be a follower, we initialize follower instance for this teen
                Follower follower = new Follower();
                follower.setEmail(user.getEmail());
                follower.setUser(user);

                user.setFollower(follower);
            } else if (user.getType() == UserType.FOLLOWER.ordinal()) {
                Follower follower = user.getFollower();
                follower.setUser(user);
                follower.setEmail(user.getEmail());
            }

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

    private static boolean isValidUser(User user) {
        if (DEBUG) sLogger.debug("Validating User");

        boolean valid = false;
        if (user != null) {
            valid = isValidEmail(user.getEmail()) && isValidString(user.getFirstName());

            if (valid) {
                // if login from Facebook, it should have facebook id
                if (user.getProvider() == SignInProvider.FACEBOOK.ordinal()) {
                    valid = isValidString(user.getFacebookId());
                    // if login from Application, it should have password
                } else if (user.getProvider() == SignInProvider.APPLICATION.ordinal()) {
                    valid = isValidString(user.getPassword());
                } else {
                    valid = false;
                }
            }

            // if all informations from User are valid and user is a Teen
            if (valid && user.getType() == UserType.TEEN.ordinal()) {
                valid = isValidTeen(user.getTeen());
            }
            // FIXME - maybe do Follower validation?
        }

        return valid;
    }

    private static boolean isValidTeen(Teen teen) {
        if (DEBUG) sLogger.debug("Validating Teen");

        return teen != null && isValidDate(teen.getBirthday())
                && isValidString(teen.getMedicalNumber());
    }

}
