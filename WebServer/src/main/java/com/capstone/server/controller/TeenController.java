
package com.capstone.server.controller;

import static com.capstone.server.utils.Validators.isValidEmail;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.capstone.server.dao.DeviceDao;
import com.capstone.server.dao.TeenDao;
import com.capstone.server.dao.UserDao;
import com.capstone.server.model.Device;
import com.capstone.server.model.DeviceMessage;
import com.capstone.server.model.FollowDataRequest;
import com.capstone.server.model.Follower;
import com.capstone.server.model.JsonResponse;
import com.capstone.server.model.Teen;
import com.capstone.server.model.User;
import com.capstone.server.utils.Constants.UserType;

@Controller
@RequestMapping(RestUriConstants.TEEN_CONTROLLER)
public class TeenController {

    private final static Logger sLogger = Logger.getLogger(TeenController.class);

    private final static boolean DEBUG = sLogger.isDebugEnabled();

    @Autowired
    private UserDao userDao;

    @Autowired
    private TeenDao teenDao;

    @Autowired
    private DeviceDao deviceDao;

    @RequestMapping(value = RestUriConstants.LIST, method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<User> visualizar() {
        List<User> users = (List<User>) userDao.findAll();

        System.out.println("user 22!!!");

        // FIXME - Manually ignore teen/follower list to send to Android app.
        // There is an alternative way by using JsonView to dynamically ignore
        // some fields before sending data, but I couldn't make it work
        for (User item : users) {
            if (item.getFollower() != null) {
                item.getFollower().setTeenList(null);
            } else if (item.getTeen() != null) {
                item.getTeen().setFollowerList(null);
            }
        }

        return users;
    }

    @RequestMapping(value = RestUriConstants.FOLLOW + "/" + RestUriConstants.SEND, method = RequestMethod.POST)
    public @ResponseBody JsonResponse sendFollowRequest(@RequestBody
    final FollowDataRequest followRequest) {
        if (!isValidFollowDataRequest(followRequest)) {
            sLogger.info("Invalid parameters");
            return new JsonResponse(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        // User that is to (un)follow some teen
        User user = followRequest.getUser();
        String userEmail = user.getEmail();

        // Teen that is to be (un)followed by some user
        Teen teen = followRequest.getTeen();

        // get current followers list of the teen to be followed
        Teen teenDb = teenDao.find(teen.getEmail());
        List<Follower> teenFollowersDb = teenDb.getFollowerList();

        // request is to follow a teen
        if (followRequest.getFollow()) {
            // check if this user is already following the teen
            boolean following = false;
            for (Follower item : teenFollowersDb) {
                if (item.getEmail().equals(userEmail)) {
                    following = true;
                    break;
                }
            }

            List<Follower> teenPendingFollowersDb = teen.getPendingFollowerList();

            // check if this user has already sent request to follow this teen
            boolean sentRequest = false;
            for (Follower item : teenPendingFollowersDb) {
                if (item.getEmail().equals(userEmail)) {
                    sentRequest = true;
                    break;
                }
            }

            // if user is not following this teen and request hasn't been set
            // yet
            if (!following && !sentRequest) {
                Device device = deviceDao.find(userEmail);

                DeviceMessage deviceMessage = new DeviceMessage();
                deviceMessage.setDeviceList(Arrays.asList(device));
                deviceMessage.setMessage("send follow request");
                DeviceController.asyncSend(deviceMessage);

                // add userEmail to list of pending followers for this teen
                teenPendingFollowersDb = getUpdatedList(userEmail, teenPendingFollowersDb, true);

                // save changes to database
                teenDb.setPendingFollowerList(teenPendingFollowersDb);
                teenDao.update(teenDb);
            }
            // request to unfollow teen
        } else {
            teenFollowersDb = getUpdatedList(userEmail, teenFollowersDb, false);

            // save changes to database
            teenDb.setFollowerList(teenFollowersDb);
            teenDao.update(teenDb);
        }

        // FIXME - Add appropriate return message
        return new JsonResponse(HttpStatus.OK, "good");
    }

    @RequestMapping(value = RestUriConstants.FOLLOW + "/" + RestUriConstants.SUBMIT, method = RequestMethod.POST)
    public @ResponseBody JsonResponse confirmFollowRequest(@RequestBody
    final FollowDataRequest followRequest) {
        if (!isValidFollowDataRequest(followRequest)) {
            sLogger.info("Invalid parameters");
            return new JsonResponse(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        // User that is to (un)follow some teen
        User user = followRequest.getUser();
        String userEmail = user.getEmail();

        // Teen that is to be (un)followed by some user
        Teen teen = followRequest.getTeen();

        // get current followers list of the teen to be followed
        Teen teenDb = teenDao.find(teen.getEmail());
        List<Follower> teenFollowersDb = teenDb.getFollowerList();

        teenFollowersDb = getUpdatedList(userEmail, teenFollowersDb, followRequest.getFollow());

        // save changes to database
        teenDb.setFollowerList(teenFollowersDb);
        teenDao.update(teenDb);

        // FIXME - Add appropriate return message
        return new JsonResponse(HttpStatus.OK, "good");
    }

    /**
     * API to get the list of followers updated with the given email. Use add
     * parameter as true to add new follower in list of followers, false to
     * remove
     */
    private List<Follower> getUpdatedList(String email, List<Follower> currentList, boolean add) {
        // safe add/remove the user from this teen followers list
        ListIterator<Follower> iterator = currentList.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().getEmail().equals(email)) {
                if (add) {
                    Follower newFollower = new Follower();
                    newFollower.setEmail(email);
                    iterator.add(newFollower);
                } else {
                    iterator.remove();
                }
                break;
            }
        }

        return currentList;
    }

    private static boolean isValidFollowDataRequest(FollowDataRequest followRequest) {
        if (DEBUG) sLogger.debug("Validating FollowDataRequest");

        boolean valid = false;
        if (followRequest != null && isValidUser(followRequest.getUser())
                && isValidTeen(followRequest.getTeen())) {
            valid = true;
        }

        return valid;
    }

    /** Check if User is Teen of Follower and if it has a valid e-mail */
    private static boolean isValidUser(User user) {
        if (DEBUG) sLogger.debug("Validating User");

        boolean valid = false;
        if (user != null && user.getType() != null) {
            valid = isValidEmail(user.getEmail()) && (user.getType().equals(UserType.TEEN)
                    || user.getType().equals(UserType.FOLLOWER));
        }

        return valid;
    }

    /** Check if Teen has a valid email */
    private static boolean isValidTeen(Teen teen) {
        if (DEBUG) sLogger.debug("Validating Teen");

        return teen != null && isValidEmail(teen.getEmail());
    }

}
