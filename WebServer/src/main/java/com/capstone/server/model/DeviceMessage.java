
package com.capstone.server.model;

import java.util.List;

/**
 * Auxiliary class to encapsulate a message that should be sent to a number of
 * devices from web-client to the server
 */
public class DeviceMessage {

    private List<Device> deviceList;
    private String message;

    public List<Device> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<Device> deviceList) {
        this.deviceList = deviceList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
