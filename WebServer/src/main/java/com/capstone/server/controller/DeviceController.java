
package com.capstone.server.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.servlet.ServletConfig;

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

import com.capstone.server.config.ApiKeyInitializer;
import com.capstone.server.dao.DeviceDao;
import com.capstone.server.dao.UserDao;
import com.capstone.server.model.Device;
import com.capstone.server.model.DeviceMessage;
import com.capstone.server.model.JsonResponse;
import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

@Controller
@RequestMapping(RestUriConstants.DEVICE_CONTROLLER)
public class DeviceController {

    private final static Logger sLogger = Logger.getLogger(DeviceController.class);

    private final static boolean DEBUG = sLogger.isDebugEnabled();

    private final Executor sThreadPool = Executors.newFixedThreadPool(5);

    @Autowired
    private DeviceDao deviceDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ServletConfig servletConfig;

    private static Sender sender;

    @PostConstruct
    public void init() {
        sender = newSender(servletConfig);
    }

    /**
     * Creates the {@link Sender} based on the servlet settings.
     */
    protected Sender newSender(ServletConfig config) {
        String key = (String) config.getServletContext().getAttribute(
                ApiKeyInitializer.ATTRIBUTE_ACCESS_KEY);
        return new Sender(key);
    }

    @RequestMapping(value = RestUriConstants.REGISTER, method = RequestMethod.POST)
    public @ResponseBody JsonResponse register(@RequestBody final Device device) {
        if (DEBUG) {
            sLogger.debug("Registering GCM device: " + device.getToken() + " E-mail: "
                    + device.getEmail());
        }

        if (deviceDao.find(device.getEmail()) == null) {
            deviceDao.persist(device);
        } else {
            deviceDao.update(device);
        }

        return new JsonResponse(HttpStatus.OK, "success");
    }

    @RequestMapping(value = RestUriConstants.DELETE, method = RequestMethod.DELETE)
    public @ResponseBody JsonResponse unregister(@PathVariable(RestUriConstants.PARAM_ID) String token) {
        if (DEBUG) {
            sLogger.debug("Unregistering GCM device: " + token);
        }

        deviceDao.removeByToken(token);
        return new JsonResponse(HttpStatus.OK, "success");
    }

    @RequestMapping(RestUriConstants.VIEW)
    public String viewDevices(Model model) {
        model.addAttribute("users", userDao.findAll());
        return "device/view";
    }

    @RequestMapping(value = RestUriConstants.SEND, method = RequestMethod.POST)
    public @ResponseBody JsonResponse sendMessage(@RequestBody final DeviceMessage deviceMessage) {
        asyncSend(deviceMessage, com.capstone.server.utils.Constants.GCM_ADMIN_TYPE);
        return new JsonResponse(HttpStatus.OK, "Message successfully sent!");
    }

    public void asyncSend(final DeviceMessage deviceMessage, final String... type) {
        final List<Device> devices = new ArrayList<Device>(deviceMessage.getDeviceList());

        final List<String> devicesRegIds = new ArrayList<String>();
        for (Device item : devices) {
            devicesRegIds.add(item.getToken());
        }

        sThreadPool.execute(new Runnable() {
            public void run() {
                Builder messageBuilder = new Message.Builder().addData("message",
                        deviceMessage.getMessage()).addData("type", type[0]);

                // if check in id has been provided
                if (type[0].equals(com.capstone.server.utils.Constants.GCM_NEW_CHECK_IN_TYPE) && 
                        type[1] != null) {
                    messageBuilder.addData("checkInId", type[1]);
                }

                Message message = messageBuilder.build();

                MulticastResult multicastResult;
                try {
                    multicastResult = sender.send(message, devicesRegIds, 5);
                } catch (IOException e) {
                    sLogger.warn("Error posting messages", e);
                    return;
                }

                List<Result> results = multicastResult.getResults();
                for (int i = 0; i < devices.size(); i++) {
                    String regId = devices.get(i).getToken();
                    Result result = results.get(i);
                    String messageId = result.getMessageId();

                    if (messageId != null) {
                        sLogger.debug("Succesfully sent message to device: " + regId + "; messageId = " + messageId);
                        String canonicalRegId = result.getCanonicalRegistrationId();
                        if (canonicalRegId != null) {
                            // same device has more than on registration id - update it
                            sLogger.info("canonicalRegId " + canonicalRegId);

                            Device device = deviceDao.findByToken(regId);
                            device.setToken(canonicalRegId);
                            deviceDao.update(device);
                        }
                    } else {
                        String error = result.getErrorCodeName();
                        if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                            // application has been removed from device - unregister it
                            sLogger.info("Unregistered device: " + regId);

                            deviceDao.removeByToken(regId);
                        } else {
                            sLogger.error("Error sending message to " + regId + ": " + error);
                        }
                    }
                }
            }
        });
    }
}
