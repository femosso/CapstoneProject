
package com.capstone.server.controller;

import static com.capstone.server.utils.Validators.isValidEmail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.capstone.server.dao.DeviceDao;
import com.capstone.server.dao.TeenDao;
import com.capstone.server.dao.UserDao;
import com.capstone.server.model.Device;
import com.capstone.server.model.DeviceMessage;
import com.capstone.server.model.FollowDataRequest;
import com.capstone.server.model.FollowDataResponse;
import com.capstone.server.model.Follower;
import com.capstone.server.model.JsonResponse;
import com.capstone.server.model.Teen;
import com.capstone.server.model.TeenListRequest;
import com.capstone.server.model.User;
import com.capstone.server.utils.Constants;
import com.capstone.server.utils.Constants.SignInProvider;
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
    public @ResponseBody TeenListRequest requestTeenList(@RequestParam(RestUriConstants.PARAM_EMAIL) String email) {
        if (!isValidEmail(email)) {
            sLogger.info("Invalid parameters");
            return null;
        }

        // get user that has requested the list of teens
        User requesterDb = userDao.find(email, true);
        if (requesterDb == null) {
            sLogger.info("No such e-mail " + email);
            return null;
        }

        sLogger.info("User " + email + " requested list of teens");

        // get information of the requester, such as its teen and pending teen list
        User requester = new User();
        requester.setEmail(requesterDb.getEmail());
        requester.setType(requesterDb.getType());

        List<Teen> pendingTeenListDb = requesterDb.getFollower().getPendingTeenList();
        for (Teen item : pendingTeenListDb) {
            item.setFollowerList(null);
            item.setUser(null);
            item.setPendingFollowerList(null);
        }

        List<Teen> teenListDb = requesterDb.getFollower().getTeenList();
        for (Teen item : teenListDb) {
            item.setFollowerList(null);
            item.setUser(null);
            item.setPendingFollowerList(null);
        }

        Follower followerRequestor = new Follower();
        followerRequestor.setPendingTeenList(pendingTeenListDb);
        followerRequestor.setTeenList(teenListDb);

        requester.setFollower(followerRequestor);

        // get all teens from database
        List<User> teensListDb = (List<User>) userDao.findByType(UserType.TEEN.ordinal());

        // add only the fields that matter to be sent to android app
        User user; Teen teen;
        List<User> teensList = new ArrayList<>();
        for(User item : teensListDb) {
            if(!item.getEmail().equals(requester.getEmail())) {
                user = new User();
                user.setEmail(item.getEmail());
                user.setFirstName(item.getFirstName());
                user.setLastName(item.getLastName());

                teen = new Teen();
                teen.setBirthday(item.getTeen().getBirthday());
                teen.setMedicalNumber(item.getTeen().getMedicalNumber());

                user.setTeen(teen);

                teensList.add(user);
            }
        }

        TeenListRequest teenListRequest = new TeenListRequest();
        teenListRequest.setTeenList(teensList);
        teenListRequest.setRequester(requester);

        return teenListRequest;
    }

    @RequestMapping(value = RestUriConstants.FOLLOW + "/" + RestUriConstants.SEND, method = RequestMethod.POST)
    public @ResponseBody JsonResponse sendFollowRequest(@RequestBody final FollowDataRequest followRequest) {
        if (!isValidFollowData(followRequest)) {
            sLogger.info("Invalid parameters");
            return new JsonResponse(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        // User that is to (un)follow some teen
        User userDb = userDao.find(followRequest.getUser().getEmail(), true);

        // Teen that is to be (un)followed by some user
        Teen teen = followRequest.getTeen();
        String teenEmail = teen.getEmail();

        // get current followers list of the teen to be followed
        Teen teenDb = teenDao.find(teen.getEmail(), true);
        List<Follower> teenFollowersDb = teenDb.getFollowerList();

        // request is to follow a teen
        if (followRequest.getFollow()) {
            // check if this user is already following the teen
            boolean following = false;
            for (Follower item : teenFollowersDb) {
                if (item.getEmail().equals(userDb.getEmail())) {
                    following = true;
                    break;
                }
            }

            List<Follower> teenPendingFollowersDb = teenDb.getPendingFollowerList();

            // check if this user has already sent request to follow this teen
            boolean sentRequest = false;
            for (Follower item : teenPendingFollowersDb) {
                if (item.getEmail().equals(userDb.getEmail())) {
                    sentRequest = true;
                    break;
                }
            }

            // if user is not following this teen and request hasn't been set yet
            if (!following && !sentRequest) {
                Device device = deviceDao.find(teenEmail);

                DeviceMessage deviceMessage = new DeviceMessage();
                deviceMessage.setDeviceList(Arrays.asList(device));
                deviceMessage.setMessage(userDb.getFirstName() + " wants to follow you!");

                DeviceController deviceController = new DeviceController();
                deviceController.asyncSend(deviceMessage, Constants.GCM_FOLLOW_REQUEST_TYPE);

                // add userEmail to list of pending followers for this teen
                teenPendingFollowersDb = getUpdatedList(userDb, teenPendingFollowersDb, true);

                // save changes to database
                teenDb.setPendingFollowerList(teenPendingFollowersDb);
                teenDao.update(teenDb);
            }
        } else { // request to unfollow teen
            teenFollowersDb = getUpdatedList(userDb, teenFollowersDb, false);

            // save changes to database
            teenDb.setFollowerList(teenFollowersDb);
            teenDao.update(teenDb);
        }

        return new JsonResponse(HttpStatus.OK, "Ok");
    }

    @RequestMapping(value = RestUriConstants.PENDING + "/" + RestUriConstants.LIST, method = RequestMethod.GET)
    public @ResponseBody List<User> requestPendingFollowRequestList(
            @RequestParam(RestUriConstants.PARAM_EMAIL) String email) {
        if (!isValidEmail(email)) {
            sLogger.info("Invalid parameters");
            return null;
        }

        // get user that has requested the list of teens
        User requestorDb = userDao.find(email, true);
        if (requestorDb == null) {
            sLogger.info("No such e-mail " + email);
            return null;
        }

        // only teens should be able to request pending follow list as only them will have it
        if(requestorDb.getType() != UserType.TEEN.ordinal()) {
            sLogger.info("Only teens have pending follow request");
            return null;
        }

        sLogger.info("User " + email + " requested list pending follow request");

        List<Follower> pendingFollowerListDb = requestorDb.getTeen().getPendingFollowerList();

        List<User> usersList = new ArrayList<>(); User user;
        for (Follower follower : pendingFollowerListDb) {
            // set only fields that matter
            user = new User();
            user.setEmail(follower.getUser().getEmail());
            user.setFirstName(follower.getUser().getFirstName());
            user.setLastName(follower.getUser().getLastName());
            user.setType(follower.getUser().getType());

            usersList.add(user);
        }

        return usersList;
    }

    @RequestMapping(value = RestUriConstants.FOLLOW + "/" + RestUriConstants.SUBMIT, method = RequestMethod.POST)
    public @ResponseBody FollowDataResponse confirmFollowRequest(@RequestBody final FollowDataRequest followRequest) {
        if (!isValidFollowData(followRequest)) {
            sLogger.info("Invalid parameters");
            return new FollowDataResponse(new JsonResponse(HttpStatus.BAD_REQUEST, "Invalid parameters"), null);
        }

        // User that is waiting the teen to confirm follow request
        User userDb = userDao.find(followRequest.getUser().getEmail(), true);

        // Teen that is confirming/denying a follow request
        Teen teen = followRequest.getTeen();

        // get current followers list of the teen to be followed
        Teen teenDb = teenDao.find(teen.getEmail(), true);

        List<Follower> teenFollowersDb = teenDb.getFollowerList();
        List<Follower> teenPendingFollowersDb = teenDb.getPendingFollowerList();

        teenFollowersDb = getUpdatedList(userDb, teenFollowersDb, followRequest.getFollow());

        // remove it from pending followers list
        teenPendingFollowersDb = getUpdatedList(userDb, teenPendingFollowersDb, false);

        // save changes to database
        teenDb.setFollowerList(teenFollowersDb);
        teenDb.setPendingFollowerList(teenPendingFollowersDb);
        teenDao.update(teenDb);

        List<User> updatedUsersList = new ArrayList<>(); User user;
        for (Follower follower : teenPendingFollowersDb) {
            // set only fields that matter
            user = new User();
            user.setEmail(follower.getUser().getEmail());
            user.setFirstName(follower.getUser().getFirstName());
            user.setLastName(follower.getUser().getLastName());
            user.setType(follower.getUser().getType());

            updatedUsersList.add(user);
        }

        return new FollowDataResponse(new JsonResponse(HttpStatus.OK, "good"), updatedUsersList);
    }

    @RequestMapping(value = RestUriConstants.SHARED_DATA, method = RequestMethod.POST)
    public @ResponseBody JsonResponse updateSharedData(@RequestBody final Teen teen) {
        if (!isValidTeen(teen)) {
            sLogger.info("Invalid parameters");
            return null;
        }

        Teen teenDb = teenDao.find(teen.getEmail());
        teenDb.setSharedData(teen.getSharedData());

        teenDao.update(teenDb);

        return new JsonResponse(HttpStatus.OK, "good");
    }

    @RequestMapping(value = RestUriConstants.PHOTO, method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] retrieveTeenPhoto(
            @RequestParam(RestUriConstants.PARAM_EMAIL) String email) throws IOException {
        if (!isValidEmail(email)) {
            sLogger.info("Invalid parameters");
            return null;
        }

        User userDb = userDao.find(email);
        System.out.println("Retrieving " + email + " photo");

        if (userDb != null) {
            InputStream in = null;
            try {
                if (SignInProvider.FACEBOOK.ordinal() == userDb.getProvider()) {
                    in = downloadFacebookProfilePicture(userDb.getFacebookId());
                } else if (SignInProvider.APPLICATION.ordinal() == userDb.getProvider()) {
                    in = new FileInputStream("C:\\Users\\User\\Desktop\\images\\icon-profile.png");
                }

                if (in != null) {
                    return IOUtils.toByteArray(in);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static final String PROFILE_PICTURE_URL = "https://graph.facebook.com/%s/picture?type=large";

    private InputStream downloadFacebookProfilePicture(String uid) throws MalformedURLException, IOException {
        HttpURLConnection.setFollowRedirects(false);
        String profilePictureUrlString = new URL(String.format(PROFILE_PICTURE_URL, uid))
                .openConnection().getHeaderField("location");

        if (profilePictureUrlString != null) {
            return new URL(profilePictureUrlString).openStream();
        }
        return null;
    }

    /**
     * API to get the list of followers updated with the given email. Use add
     * parameter as true to add new follower in list of followers, false to
     * remove
     */
    private List<Follower> getUpdatedList(User user, List<Follower> currentList, boolean add) {
        if (add) {
            currentList.add(user.getFollower());
        } else {
            // safe remove the user from this teen followers list
            ListIterator<Follower> iterator = currentList.listIterator();
            while (iterator.hasNext()) {
                if (iterator.next().getEmail().equals(user.getEmail())) {
                    iterator.remove();
                    break;
                }
            }
        }

        return currentList;
    }

    private static boolean isValidFollowData(FollowDataRequest followRequest) {
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
        if (user != null) {
            valid = isValidEmail(user.getEmail());
        }

        return valid;
    }

    /** Check if Teen has a valid email */
    private static boolean isValidTeen(Teen teen) {
        if (DEBUG) sLogger.debug("Validating Teen");

        return teen != null && isValidEmail(teen.getEmail());
    }

}
