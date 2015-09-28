
package com.capstone.server.controller;

import static com.capstone.server.utils.Validators.isValidEmail;
import static com.capstone.server.utils.Validators.isValidString;
import static com.capstone.server.utils.Validators.isValidDate;

import java.util.List;
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

import com.capstone.server.dao.FollowerDao;
import com.capstone.server.dao.TeenDao;
import com.capstone.server.dao.UserDao;
import com.capstone.server.model.Follower;
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

    @Autowired
    private FollowerDao followerDao;

    private MessageSource messageSource;

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @RequestMapping(value = RestUriConstants.SEND, method = RequestMethod.POST)
    public @ResponseBody JsonResponse sendLogin(@RequestBody final User user,
            HttpSession session, Locale locale) {
        // FIXME - add field validation

        User databaseUser = userDao.find(user.getEmail());

        if (databaseUser != null) {
            if (user.getProvider().equals(SignInProvider.APPLICATION)
                    && databaseUser.getPassword().equals(user.getPassword())) {
                session.setAttribute(Constants.SESSION_USER, databaseUser);
            } else if (user.getProvider().equals(SignInProvider.FACEBOOK)
                    && databaseUser.getFacebookId().equals(user.getFacebookId())) {
                session.setAttribute(Constants.SESSION_USER, databaseUser);
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

/*        User user1 = new User();
        user1.setEmail("user1@gmail.com");
        user1.setFirstName("user1");

        Teen teen1 = new Teen();
        teen1.setMedicalNumber("12345678");
        teen1.setEmail(user1.getEmail());

        user1.setTeen(teen1);

        userDao.persist(user1);

        User user2 = new User();
        user2.setEmail("user2@gmail.com");
        user2.setFirstName("user2");

        Teen teen2 = new Teen();
        teen2.setMedicalNumber("12345678");
        teen2.setEmail(user2.getEmail());

        user2.setTeen(teen2);

        userDao.persist(user2);*/

        

/*        User user3 = new User();
        user3.setEmail("user3@gmail.com");
        user3.setFirstName("user3");
        
        Follower follower3 = new Follower();
        follower3.setEmail(user3.getEmail());
        
        user3.setFollower(follower3);
        
        userDao.persist(user3);*/
        
        Follower follower3 = followerDao.find("user3@gmail.com");

        List<Teen> teens = (List<Teen>) teenDao.findAll();
        follower3.setTeenList(teens);

        followerDao.update(follower3);

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
            if (user.getType().equals(UserType.TEEN)) {
                Teen teen = user.getTeen();
                teen.setEmail(user.getEmail());
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
        if (user != null && user.getType() != null) {
            valid = isValidEmail(user.getEmail()) && isValidString(user.getFirstName());

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

}
