
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
import com.capstone.server.model.Device;
import com.capstone.server.model.DeviceMessage;
import com.capstone.server.model.Follower;
import com.capstone.server.model.JsonResponse;
import com.capstone.server.model.Question;
import com.capstone.server.model.Teen;
import com.capstone.server.model.User;
import com.capstone.server.utils.Constants;
import com.capstone.server.utils.Constants.QuestionType;

@Controller
@RequestMapping(RestUriConstants.CHECK_IN_CONTROLLER)
public class CheckInController {

    private final static Logger sLogger = Logger.getLogger(CheckInController.class);

    @Autowired
    private CheckInDao checkInDao;

    @Autowired
    private UserDao userDao;

    @RequestMapping(value = RestUriConstants.LIST, method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<CheckIn> requestCheckInList(@RequestParam(RestUriConstants.PARAM_EMAIL) String email) {
        if (!isValidEmail(email)) {
            sLogger.info("Invalid parameters");
            return null;
        }

        // get user that has requested the list of latest check ins
        User requesterDb = userDao.find(email, true);
        if (requesterDb == null) {
            sLogger.info("No such e-mail " + email);
            return null;
        }

        sLogger.info("User " + email + " requested list of latest check ins");

        List<CheckIn> checkIns = new ArrayList<>();

        // retrieve all teens that the requester user is following
        List<Teen> teenListDb = requesterDb.getFollower().getTeenList();
        for (Teen teenDb : teenListDb) {
            // get all the check ins of each user the requester is following
            List<CheckIn> checkInsDb = (List<CheckIn>) checkInDao.findByTeen(teenDb.getEmail());

            // add only the fields that matter to be sent to android app
            CheckIn checkIn; User user;
            for (CheckIn checkInDb : checkInsDb) {
                user = new User();
                user.setEmail(checkInDb.getUser().getEmail());
                user.setFirstName(checkInDb.getUser().getFirstName());
                user.setLastName(checkInDb.getUser().getLastName());

                checkIn = new CheckIn();
                checkIn.setUser(user);

                checkIn.setId(checkInDb.getId());
                checkIn.setDate(checkInDb.getDate());

                // set answer list without general questions
                checkIn.setAnswerList(buildAnswerList(checkInDb, teenDb, false));
                checkIns.add(checkIn);
            }
        }

        return checkIns;
    }

    @RequestMapping(value = RestUriConstants.VIEW, method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody CheckIn requestCheckIn(@RequestParam(RestUriConstants.PARAM_ID) long id) {
        CheckIn checkInDb = checkInDao.find(id, true);

        if (checkInDb == null) {
            sLogger.debug("No such check in with id " + id);
            return null;
        }

        // add only the fields that matter to be sent to android app
        Teen teen; User user; CheckIn checkIn;

        Teen teenDb = checkInDb.getUser().getTeen();

        teen = new Teen();
        teen.setBirthday(teenDb.getBirthday());
        teen.setMedicalNumber(teenDb.getMedicalNumber());

        user = new User();
        user.setEmail(checkInDb.getUser().getEmail());
        user.setFirstName(checkInDb.getUser().getFirstName());
        user.setLastName(checkInDb.getUser().getLastName());
        user.setTeen(teen);

        checkIn = new CheckIn();
        checkIn.setUser(user);
        checkIn.setId(checkInDb.getId());
        checkIn.setDate(checkInDb.getDate());

        // set answer list including general questions
        checkIn.setAnswerList(buildAnswerList(checkInDb, teenDb, true));

        return checkIn;
    }

    @RequestMapping(value = RestUriConstants.SEND, method = RequestMethod.POST)
    public @ResponseBody JsonResponse sendCheckIn(@RequestPart(RestUriConstants.PARAM_CHECK_IN) CheckIn checkIn,
            @RequestPart(value = RestUriConstants.PARAM_PHOTO, required = false) MultipartFile file) {
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

        // notify all the followers of the user which has just done the check in
        User userDb = userDao.find(checkIn.getUser().getEmail(), true);

        // list of device which will be notified about the new check in
        List<Device> deviceList = new ArrayList<>();

        List<Follower> followers = userDb.getTeen().getFollowerList();
        for(Follower followerDb : followers) {
            deviceList.add(followerDb.getUser().getDevice());
        }

        DeviceMessage deviceMessage = new DeviceMessage();
        deviceMessage.setDeviceList(deviceList);
        deviceMessage.setMessage(userDb.getFirstName() + " has published a new check-in!");

        DeviceController deviceController = new DeviceController();
        deviceController.asyncSend(deviceMessage, Constants.GCM_NEW_CHECK_IN_TYPE, String.valueOf(checkIn.getId()));

        return new JsonResponse(HttpStatus.OK, "ok");
    }

    @RequestMapping(value = RestUriConstants.PHOTO, method = RequestMethod.GET)
    public @ResponseBody FileSystemResource retrieveCheckInPhoto(@RequestParam(RestUriConstants.PARAM_ID) long id) {
        CheckIn checkInDb = checkInDao.find(id);
        if (checkInDb.getPhotoPath() != null) {
            return new FileSystemResource(checkInDb.getPhotoPath());
        }

        return null;
    }

    private List<Answer> buildAnswerList(CheckIn checkInDb, Teen teenDb, boolean includeGeneralQuestions) {
        Question question; Answer answer;
        List<Answer> answers = new ArrayList<>();

        for (Answer answerDb : checkInDb.getAnswerList()) {
            String questionType = answerDb.getQuestion().getType();

            // only add answer information if teen desires to shared this kind of data.
            // when includeGeneralQuestions parameter is false, we do not add
            // QuestionType.TYPE3 questions/answers
            if (questionType != null && teenDb.getSharedDataAsList().contains(questionType)
                    && (includeGeneralQuestions || !questionType.equals(QuestionType.TYPE3.getValue()))) {
                question = new Question();
                question.setType(questionType);
                question.setText(answerDb.getQuestion().getText());

                String questionFormat = answerDb.getQuestion().getFormat();

                answer = new Answer();
                answer.setQuestion(question);

                // format answer if question is in multiple choice format
                String answerText = questionFormat != null
                        && questionFormat.equals(Constants.QuestionFormat.FORMAT1.getValue())
                                ? formatAnswersList(answerDb.getText()) : answerDb.getText();
                answer.setText(answerText);

                answers.add(answer);
            }
        }

        return answers;
    }

    /**
     * Auxiliary method to format answers of list type (comma separated) in a
     * good way to be shown in Android app
     */
    private String formatAnswersList(String value) {
        if(value != null && value.endsWith(",")) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }

    private boolean isValidCheckIn(CheckIn checkIn) {
        return true;
    }
}
