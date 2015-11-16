
package com.capstone.server.controller;

import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.capstone.server.utils.Constants;

@Controller
public class HomeController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "home";
    }

    @RequestMapping(value = RestUriConstants.CLEAR, method = RequestMethod.POST)
    public @ResponseBody void clearSession(HttpSession session) {
        session.removeAttribute(Constants.IS_REDIRECT);
    }
}
