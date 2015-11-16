
package com.capstone.server.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.capstone.server.dao.AnswerDao;
import com.capstone.server.dao.UserDao;
import com.capstone.server.model.Answer;
import com.capstone.server.model.CheckIn;
import com.capstone.server.model.Feedback;
import com.capstone.server.model.User;

@Controller
@RequestMapping(RestUriConstants.ANSWER_CONTROLLER)
public class AnswerController {

    private final static Logger sLogger = Logger.getLogger(AnswerController.class);

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private UserDao userDao;

    @RequestMapping(value = RestUriConstants.HISTORIC, method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody Feedback requestHistoric(@RequestParam(RestUriConstants.PARAM_EMAIL) String email,
            @RequestParam(RestUriConstants.PARAM_TYPE) String type) {
        sLogger.debug("Historic request for " + email + " and question type " + type);

        User userDb = userDao.find(email, true);
        List<Answer> answersDb = (List<Answer>) answerDao.findByTeenAndType(email, type);

        // add only the fields that matter to be sent to android app
        Answer answer; CheckIn checkIn;
        List<Answer> answers = new ArrayList<>();
        for (Answer answerDb : answersDb) {
            checkIn = new CheckIn();
            checkIn.setDate(answerDb.getCheckIn().getDate());

            answer = new Answer();
            answer.setId(answerDb.getId());
            answer.setText(answerDb.getText());
            answer.setCheckIn(checkIn);

            answers.add(answer);
        }

        User user = new User();
        user.setFirstName(userDb.getFirstName());
        user.setLastName(userDb.getLastName());

        return new Feedback(user, answers);
    }
}
