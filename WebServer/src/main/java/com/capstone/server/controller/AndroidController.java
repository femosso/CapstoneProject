package com.capstone.server.controller;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.capstone.server.model.User;
import com.capstone.server.utils.Constants;

@Controller
@RequestMapping(RestUriConstants.ANDROID_CONTROLLER)
public class AndroidController {

    private final static Logger sLogger = Logger.getLogger(AndroidController.class);

    @RequestMapping(value = RestUriConstants.VIEW, method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody User visualizar(HttpSession session) {
        sLogger.info("android/view");

        User user = new User();

        if (session.getAttribute(Constants.SESSION_USER) != null) {
            user.setEmail("tem session");
        } else {
            user.setEmail("NAO tem session");
        }
        return user;
    }
}
