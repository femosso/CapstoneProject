
package com.capstone.server.controller;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.capstone.server.dao.QuestionDao;
import com.capstone.server.model.JsonResponse;
import com.capstone.server.model.Question;
import com.capstone.server.utils.Constants.QuestionFormat;
import com.capstone.server.utils.Constants.QuestionType;

@Controller
@RequestMapping(RestUriConstants.QUESTION_CONTROLLER)
public class QuestionController {

    private final static Logger sLogger = Logger.getLogger(AndroidController.class);

    private final static boolean DEBUG = sLogger.isDebugEnabled();

    @Autowired
    private QuestionDao questionDao;

    private MessageSource messageSource;

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @RequestMapping(RestUriConstants.REGISTER)
    public String showRegisterForm(Model model) {
        model.addAttribute("formats", QuestionFormat.names());
        model.addAttribute("types", QuestionType.names());
        return "question/register";
    }

    @RequestMapping(RestUriConstants.VIEW)
    public String viewQuestions(Model model) {
        model.addAttribute("formats", QuestionFormat.names());
        model.addAttribute("types", QuestionType.names());
        model.addAttribute("questions", questionDao.findAll());
        return "question/view";
    }

    @RequestMapping(RestUriConstants.REGISTER + "/" + RestUriConstants.SUBMIT)
    public @ResponseBody JsonResponse registerFormSubmit(@RequestBody final Question question, Locale locale) {

        if (!isValidQuestion(question)) {
            sLogger.info("Invalid parameters");
            return new JsonResponse(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        // set locale to the one configured in the server
        question.setLocale(locale.toLanguageTag());

        if(question.getId() == null) {
            questionDao.persist(question);
        } else {
            questionDao.update(question);
        }

        return new JsonResponse(HttpStatus.OK, "ok");
    }

    private boolean isValidQuestion(Question question) {
        return true;
    }
}
