
package com.capstone.server.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    public @ResponseBody JsonResponse sendLogin(@RequestBody
    final User user, HttpSession session, Locale locale) {

        User databaseUser = userDao.find(user.getEmail());

        if (databaseUser != null) {
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
                    new Object[] {
                        user.getEmail()
                    }, locale);
            sLogger.info(output);

            return new JsonResponse(HttpStatus.OK, output);
        } else {
            output = messageSource.getMessage("label.loginController.loginFail",
                    new Object[] {
                        user.getEmail()
                    }, locale);
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
    public @ResponseBody JsonResponse registerFormSubmit(@RequestBody
    final User user, Locale locale) {

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
            if (user.getType().equals(UserType.TEEN)) {
                Teen teen = user.getTeen();
                teen.setUser(user);
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

    private static boolean isValidString(String str) {
        boolean ret = str != null && !str.trim().isEmpty();
        if (DEBUG) sLogger.debug("isValidString(" + str + ") " + ret);
        return ret;
    }

    private static boolean isValidUser(User user) {
        if (DEBUG) sLogger.debug("Validating User");

        boolean valid = false;
        if (user != null && user.getType() != null) {
            valid = isValidString(user.getEmail()) && isValidString(user.getFirstName());

            if (valid && user.getProvider() != null) {
                // if login from Facebook, it should have facebook id
                if (user.getProvider().equals(SignInProvider.FACEBOOK)) {
                    valid = isValidString(user.getFacebookId());
                    // if login from Application, it should have password
                } else if (user.getProvider().equals(SignInProvider.APPLICATION)) {
                    valid = isValidString(user.getPassword());
                }
            }

            // if all informations from Follower are valid and user is a Teen
            if (valid && user.getType().equals(UserType.TEEN)) {
                valid = isValidTeen(user.getTeen());
            }
        }

        return valid;
    }

    private static boolean isValidTeen(Teen teen) {
        if (DEBUG) sLogger.debug("Validating Teen");

        return teen != null && isValidDate(teen.getBirthday())
                && isValidString(teen.getMedicalNumber());
    }

    private static boolean isValidDate(String date) {
        boolean ret = false;
        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            simpleDateFormat.setLenient(false);
            try {
                simpleDateFormat.parse(date);
                ret = true;
            } catch (ParseException e) {
            }
        }
        if (DEBUG) sLogger.debug("isValidDate(" + date + ") " + ret);
        return ret;
    }
}
