
package com.capstone.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.capstone.server.dao.QuestionDao;
import com.capstone.server.model.Alternative;
import com.capstone.server.model.JsonResponse;
import com.capstone.server.model.Question;
import com.capstone.server.utils.Constants.QuestionFormat;
import com.capstone.server.utils.Constants.QuestionType;

@Controller
@RequestMapping(RestUriConstants.QUESTION_CONTROLLER)
public class QuestionController {

    private final static Logger sLogger = Logger.getLogger(QuestionController.class);

    private final static boolean DEBUG = sLogger.isDebugEnabled();

    @Autowired
    private QuestionDao questionDao;

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
        model.addAttribute("questions", questionDao.findAll(true));

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

        if(question.getFormat().equals(QuestionFormat.FORMAT1.getValue())) {
            for(Alternative item : question.getAlternativeList()) {
                item.setQuestion(question);
            }
        }

        if(question.getId() == null) {
            questionDao.persist(question);
        } else {
            // re-set the answer and alternative list otherwise Hibernate will
            // think we want to empty those lists
            Question questionDb = questionDao.find(question.getId(), true);
            question.setAnswerList(questionDb.getAnswerList());

            // only re-set alternative list if not of multiple-choice format,
            // otherwise this would be done in the loop before
            if(!question.getFormat().equals(QuestionFormat.FORMAT1.getValue())) {
                question.setAlternativeList(questionDb.getAlternativeList());
            }

            questionDao.update(question);
        }

        return new JsonResponse(HttpStatus.OK, "ok");
    }

    @RequestMapping(value = RestUriConstants.LIST, method = RequestMethod.GET)
    public @ResponseBody List<Question> requestQuestion() {
        List<Question> questionListDb = (List<Question>) questionDao.findAll(true);
        List<Question> questionList = new ArrayList<>();

        if(questionListDb != null) {
            Question question;

            // get only relevant fields from question
            for(Question item : questionListDb) {
                question = new Question();
                question.setId(item.getId());
                question.setText(item.getText());
                question.setType(item.getType());
                question.setFormat(item.getFormat());
                question.setAlternativeList(item.getAlternativeList());

                questionList.add(question);
            }
        }

        return questionList;
    }

    @RequestMapping(value = RestUriConstants.DELETE, method = RequestMethod.DELETE)
    public @ResponseBody JsonResponse delete(@PathVariable("id") long id) {
        questionDao.remove(id);
        return new JsonResponse(HttpStatus.OK, "ok");
    }

    private boolean isValidQuestion(Question question) {
        return true;
    }

/*    private boolean isValidCheckIn(CheckIn checkIn) {
        return true;
    }*/
}
