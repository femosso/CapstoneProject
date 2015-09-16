package com.capstone.server.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.capstone.server.model.User;

@Controller
@RequestMapping(RestUriConstants.ANDROID_CONTROLLER)
public class AndroidController {

    private final static Logger sLogger = Logger.getLogger(AndroidController.class);

    @RequestMapping(value = RestUriConstants.VIEW, method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody User visualizar() {
        sLogger.info("android/view");

        return new User("felipemosso@gmail.com", "1234");
    }
}
