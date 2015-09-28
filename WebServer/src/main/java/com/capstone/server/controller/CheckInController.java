
package com.capstone.server.controller;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.capstone.server.dao.CheckInDao;
import com.capstone.server.dao.UserDao;
import com.capstone.server.model.CheckIn;
import com.capstone.server.model.User;

@Controller
@RequestMapping(RestUriConstants.CHECK_IN_CONTROLLER)
public class CheckInController {

    private final static Logger sLogger = Logger.getLogger(CheckInController.class);

    @Autowired
    private CheckInDao checkInDao;

    @Autowired
    private UserDao userDao;

    @RequestMapping(value = RestUriConstants.LIST, method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<CheckIn> visualizar() {
        System.out.println("visualizar");

        User user = new User();
        user.setEmail("bababba@gmail.com");

        userDao.persist(user);
        return null;
    }

    @RequestMapping(value = RestUriConstants.LIST + "1", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<CheckIn> visualizar1() {

        System.out.println("visualizar1");

        User user = userDao.find("bababba@gmail.com");

        CheckIn checkIn1 = new CheckIn();
        checkIn1.setDate(new Date());
        checkIn1.setUser(user);
        checkInDao.persist(checkIn1);

        CheckIn checkIn2 = new CheckIn();
        checkIn2.setDate(new Date());
        checkIn2.setUser(user);
        checkInDao.persist(checkIn2);

        return null;
    }

    @RequestMapping(value = RestUriConstants.LIST + "2", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<CheckIn> visualizar2() {

        System.out.println("visualizar2");

        User user = userDao.find("bababba@gmail.com", true);

        List<CheckIn> list = user.getCheckInList();
        for (CheckIn item : list) {
            System.out.println("check in -> " + item.getDate().toString());
        }
        return null;
    }

}
