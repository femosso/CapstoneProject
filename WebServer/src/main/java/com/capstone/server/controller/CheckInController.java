
package com.capstone.server.controller;

import static com.capstone.server.utils.Validators.isValidEmail;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.capstone.server.dao.CheckInDao;
import com.capstone.server.dao.UserDao;
import com.capstone.server.model.Answer;
import com.capstone.server.model.CheckIn;
import com.capstone.server.model.JsonResponse;
import com.capstone.server.model.Question;
import com.capstone.server.model.Teen;
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
    public @ResponseBody List<CheckIn> requestCheckInList(@RequestParam("email") String email) {
        if (!isValidEmail(email)) {
            sLogger.info("Invalid parameters");
            return null;
        }

        // get user that has requested the list of latest check ins
        User requestorDb = userDao.find(email, true);
        if (requestorDb == null) {
            sLogger.info("No such e-mail " + email);
            return null;
        }

        sLogger.info("User " + email + " requested list of latest check ins");

        List<CheckIn> checkInsDb = (List<CheckIn>) checkInDao.findAll(true);

        // add only the fields that matter to be sent to android app
        CheckIn checkIn; Teen teen; User user;
        List<CheckIn> checkIns = new ArrayList<>();
        for (CheckIn checkInDb : checkInsDb) {
            teen = new Teen();
            teen.setBirthday(checkInDb.getUser().getTeen().getBirthday());
            teen.setMedicalNumber(checkInDb.getUser().getTeen().getMedicalNumber());

            user = new User();
            user.setEmail(checkInDb.getUser().getEmail());
            user.setFirstName(checkInDb.getUser().getFirstName());
            user.setTeen(teen);

            checkIn = new CheckIn();
            checkIn.setUser(user);

            checkIn.setId(checkInDb.getId());
            checkIn.setDate(checkInDb.getDate());

            checkIns.add(checkIn);
        }

        return checkIns;
    }

    @RequestMapping(value = RestUriConstants.VIEW, method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody CheckIn requestCheckIn(@RequestParam("id") long id) {
        CheckIn checkInDb = checkInDao.find(id, true);

        // add only the fields that matter to be sent to android app
        Teen teen; User user; CheckIn checkIn;
        Answer answer; List<Answer> answers; Question question;

        teen = new Teen();
        teen.setBirthday(checkInDb.getUser().getTeen().getBirthday());
        teen.setMedicalNumber(checkInDb.getUser().getTeen().getMedicalNumber());

        user = new User();
        user.setEmail(checkInDb.getUser().getEmail());
        user.setFirstName(checkInDb.getUser().getFirstName());
        user.setLastName(checkInDb.getUser().getLastName());
        user.setTeen(teen);

        checkIn = new CheckIn();
        checkIn.setUser(user);
        checkIn.setId(checkInDb.getId());
        checkIn.setDate(checkInDb.getDate());

        answers = new ArrayList<>();
        for (Answer answerDb : checkInDb.getAnswerList()) {
            question = new Question();
            question.setText(answerDb.getQuestion().getText());
            question.setType(answerDb.getQuestion().getType());

            answer = new Answer();
            answer.setText(answerDb.getText());
            answer.setQuestion(question);

            answers.add(answer);
        }
        checkIn.setAnswerList(answers);

        return checkIn;
    }

    @RequestMapping(value = RestUriConstants.SEND, method = RequestMethod.POST)
    public @ResponseBody JsonResponse sendCheckIn(@RequestPart("checkIn") CheckIn checkIn,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        if (!isValidCheckIn(checkIn)) {
            sLogger.info("Invalid parameters");
            return new JsonResponse(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        if (file != null) {
            try {
                byte[] bytes = file.getBytes();

                // Creating the directory to store file
                String rootPath = System.getProperty("catalina.home");
                File dir = new File(rootPath + File.separator + "tmpFiles");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // Create the file on server
                File serverFile = new File(dir.getAbsolutePath() + File.separator + file.getOriginalFilename());
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                stream.write(bytes);
                stream.close();

                checkIn.setPhotoPath(serverFile.getAbsolutePath());
            } catch (Exception e) {
                return new JsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error");
            }
        }

        for(Answer item : checkIn.getAnswerList()) {
            item.setCheckIn(checkIn);
        }

        checkInDao.persist(checkIn);

        return new JsonResponse(HttpStatus.OK, "ok");
    }

    @RequestMapping(value = "photo/{id}", method = RequestMethod.GET)
    public @ResponseBody FileSystemResource retrieveCheckInPhoto(@PathVariable("id") long id) {
        CheckIn checkInDb = checkInDao.find(id);
        System.out.println("id " + id + " path " + checkInDb.getPhotoPath());

        if (checkInDb.getPhotoPath() != null) {
            return new FileSystemResource(checkInDb.getPhotoPath());
        }
        return null;
    }

    private boolean isValidCheckIn(CheckIn checkIn) {
        return true;
    }
}
