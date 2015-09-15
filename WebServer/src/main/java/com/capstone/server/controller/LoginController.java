
package com.capstone.server.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.capstone.server.dao.UserDao;
import com.capstone.server.model.User;
import com.capstone.server.utils.Constants;

@Controller
@RequestMapping(RestUriConstants.LOGIN_CONTROLLER)
public class LoginController {

    private final static Logger sLogger = Logger.getLogger(LoginController.class);

    @Autowired
    private UserDao userDao;

    @RequestMapping(RestUriConstants.REGISTER)
    public String register() {
        return "login/form";
    }

    @RequestMapping(value = RestUriConstants.SEND, method = RequestMethod.POST)
    public @ResponseBody User send(@RequestBody final User user, HttpSession session) {
        session.setAttribute(Constants.SESSION_USER, user);
        return user;
    }

    @RequestMapping(RestUriConstants.SUBMIT)
    public String submit(HttpServletRequest request) {
        return "redirect:/";
    }

    @RequestMapping(RestUriConstants.LOGOUT)
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
