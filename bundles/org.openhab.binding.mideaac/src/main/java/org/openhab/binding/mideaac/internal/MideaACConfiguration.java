/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mideaac.internal;

import org.apache.commons.lang.StringUtils;

/**
 * The {@link MideaACConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jacek Dobrowolski
 */
public class MideaACConfiguration {
    /**
     * IP Address of the device.
     */
    private String ipAddress;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    private String ipPort;

    public String getIpPort() {
        return ipPort;
    }

    public void setIpPort(String ipPort) {
        this.ipPort = ipPort;
    }

    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    private int pollingTime;

    public int getPollingTime() {
        return pollingTime;
    }

    public void setDeviceId(int pollingTime) {
        this.pollingTime = pollingTime;
    }

    private boolean promptTone;

    public boolean getPromptTone() {
        return promptTone;
    }

    public void setPromptTone(boolean promptTone) {
        this.promptTone = promptTone;
    }

    public boolean isValid() {
        if (StringUtils.isBlank(ipAddress)) {
            return false;
        }
        if (StringUtils.isBlank(ipPort)) {
            return false;
        }
        if (StringUtils.isBlank(deviceId) || deviceId.equalsIgnoreCase("0")) {
            return false;
        }
        return true;
    }

    public boolean isDiscoveryNeeded() {
        if (StringUtils.isBlank(ipAddress) || !Utils.validateIP(ipAddress)) {
            return false;
        }
        if (StringUtils.isBlank(ipPort)) {
            return true;
        }
        if (StringUtils.isBlank(deviceId) || deviceId.equalsIgnoreCase("0")) {
            return true;
        }
        return true;
    }

}
