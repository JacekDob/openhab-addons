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
package org.openhab.binding.mideaac.internal.handler;

import static org.openhab.binding.mideaac.internal.MideaACBindingConstants.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mideaac.internal.MideaACConfiguration;
import org.openhab.binding.mideaac.internal.Utils;
import org.openhab.binding.mideaac.internal.discovery.DiscoveryHandler;
import org.openhab.binding.mideaac.internal.discovery.MideaACDiscoveryService;
import org.openhab.binding.mideaac.internal.handler.CommandBase.FanSpeed;
import org.openhab.binding.mideaac.internal.handler.CommandBase.OperationalMode;
import org.openhab.binding.mideaac.internal.handler.CommandBase.SwingMode;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MideaACHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jacek Dobrowolski
 */
@NonNullByDefault
public class MideaACHandler extends BaseThingHandler implements DiscoveryHandler {

    private final Logger logger = LoggerFactory.getLogger(MideaACHandler.class);

    private @Nullable MideaACConfiguration config;
    private String ipAddress = null;
    private String ipPort = null;
    private String deviceId = null;

    private static final StringType OPERATIONAL_MODE_OFF = new StringType("OFF");
    private static final StringType OPERATIONAL_MODE_AUTO = new StringType("AUTO");
    private static final StringType OPERATIONAL_MODE_COOL = new StringType("COOL");
    private static final StringType OPERATIONAL_MODE_DRY = new StringType("DRY");
    private static final StringType OPERATIONAL_MODE_HEAT = new StringType("HEAT");
    private static final StringType OPERATIONAL_MODE_FAN_ONLY = new StringType("FAN_ONLY");

    private static final StringType FAN_SPEED_OFF = new StringType("OFF");
    private static final StringType FAN_SPEED_SILENT = new StringType("SILENT");
    private static final StringType FAN_SPEED_LOW = new StringType("LOW");
    private static final StringType FAN_SPEED_MEDIUM = new StringType("MEDIUM");
    private static final StringType FAN_SPEED_HIGH = new StringType("HIGH");
    private static final StringType FAN_SPEED_AUTO = new StringType("AUTO");

    private static final StringType SWING_MODE_OFF = new StringType("OFF");
    private static final StringType SWING_MODE_VERTICAL = new StringType("VERTICAL");
    private static final StringType SWING_MODE_HORIZONTAL = new StringType("HORIZONTAL");
    private static final StringType SWING_MODE_BOTH = new StringType("BOTH");

    private ConnectionManager connectionManager;

    private ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    private Response getLastResponse() {
        return getConnectionManager().getLastResponse();
    }

    public MideaACHandler(Thing thing, String ipv4Address) {
        super(thing);
        this.thing = thing;

        connectionManager = new ConnectionManager(ipv4Address);
    }

    @Override
    public void dispose() {
        super.dispose();
        getConnectionManager().dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling channelUID {} with command {}", channelUID.getId(), command.toString());

        if (command instanceof RefreshType) {
            connectionManager.requestStatus(true);
            return;
        }

        if (channelUID.getId().equals(CHANNEL_POWER)) {
            handlePower(command);
        } else if (channelUID.getId().equals(CHANNEL_OPERATIONAL_MODE)) {
            handleOperationalMode(command);
        } else if (channelUID.getId().equals(CHANNEL_TARGET_TEMPERATURE)) {
            handleTargetTemperature(command);
        } else if (channelUID.getId().equals(CHANNEL_FAN_SPEED)) {
            handleFanSpeed(command);
        } else if (channelUID.getId().equals(CHANNEL_ECO_MODE)) {
            handleEcoMode(command);
        } else if (channelUID.getId().equals(CHANNEL_TURBO_MODE)) {
            handleTurboMode(command);
        } else if (channelUID.getId().equals(CHANNEL_SWING_MODE)) {
            handleSwingMode(command);
        } else if (channelUID.getId().equals(CHANNEL_SCREEN_DISPLAY)) {
            handleScreenDisplay(command);
        } else if (channelUID.getId().equals(CHANNEL_TEMP_UNIT)) {
            handleTempUnit(command);
        }
    }

    public void handlePower(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command.equals(OnOffType.OFF)) {
            commandSet.setPowerState(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setPowerState(true);
        } else {
            logger.debug("Unknown power state command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    public void handleOperationalMode(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        commandSet.setPowerState(true);

        if (command instanceof StringType) {
            if (command.equals(OPERATIONAL_MODE_OFF)) {
                commandSet.setPowerState(false);
                return;
            } else if (command.equals(OPERATIONAL_MODE_AUTO)) {
                commandSet.setOperationalMode(OperationalMode.AUTO);
            } else if (command.equals(OPERATIONAL_MODE_COOL)) {
                commandSet.setOperationalMode(OperationalMode.COOL);
            } else if (command.equals(OPERATIONAL_MODE_DRY)) {
                commandSet.setOperationalMode(OperationalMode.DRY);
            } else if (command.equals(OPERATIONAL_MODE_HEAT)) {
                commandSet.setOperationalMode(OperationalMode.HEAT);
            } else if (command.equals(OPERATIONAL_MODE_FAN_ONLY)) {
                commandSet.setOperationalMode(OperationalMode.FAN_ONLY);
            } else {
                logger.debug("Unknown operational mode command: {}", command);
                return;
            }
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    private static float convertTargetTemperatureToInRange(float temperature) {
        if (temperature < 17.0f) {
            return 17.0f;
        }
        if (temperature > 30.0f) {
            return 30.0f;
        }

        return temperature;
    }

    public void handleTargetTemperature(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command instanceof DecimalType) {
            commandSet.setPowerState(true);
            commandSet.setTargetTemperature(convertTargetTemperatureToInRange(((DecimalType) command).floatValue()));
            getConnectionManager().sendCommandAndMonitor(commandSet);
        } else if (command instanceof QuantityType) {
            commandSet.setPowerState(true);
            commandSet.setTargetTemperature(convertTargetTemperatureToInRange(((QuantityType) command).floatValue()));
            getConnectionManager().sendCommandAndMonitor(commandSet);
        } else {
            logger.debug("handleTargetTemperature unsupported commandType:{}", command.getClass().getTypeName());
        }

    }

    public void handleFanSpeed(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command instanceof StringType) {
            commandSet.setPowerState(true);
            if (command.equals(FAN_SPEED_OFF)) {
                commandSet.setPowerState(false);
            } else if (command.equals(FAN_SPEED_SILENT)) {
                commandSet.setFanSpeed(FanSpeed.SILENT);
            } else if (command.equals(FAN_SPEED_LOW)) {
                commandSet.setFanSpeed(FanSpeed.LOW);
            } else if (command.equals(FAN_SPEED_MEDIUM)) {
                commandSet.setFanSpeed(FanSpeed.MEDIUM);
            } else if (command.equals(FAN_SPEED_HIGH)) {
                commandSet.setFanSpeed(FanSpeed.HIGH);
            } else if (command.equals(FAN_SPEED_AUTO)) {
                commandSet.setFanSpeed(FanSpeed.AUTO);
            } else {
                logger.debug("Unknown fan speed command: {}", command);
                return;
            }
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    public void handleEcoMode(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command.equals(OnOffType.OFF)) {
            commandSet.setEcoMode(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setEcoMode(true);
        } else {
            logger.debug("Unknown eco mode command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    public void handleSwingMode(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        commandSet.setPowerState(true);

        if (command instanceof StringType) {
            if (command.equals(SWING_MODE_OFF)) {
                commandSet.setSwingMode(SwingMode.OFF);
            } else if (command.equals(SWING_MODE_VERTICAL)) {
                commandSet.setSwingMode(SwingMode.VERTICAL);
            } else if (command.equals(SWING_MODE_HORIZONTAL)) {
                commandSet.setSwingMode(SwingMode.HORIZONTAL);
            } else if (command.equals(SWING_MODE_BOTH)) {
                commandSet.setSwingMode(SwingMode.BOTH);
            } else {
                logger.debug("Unknown swing mode command: {}", command);
                return;
            }
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    public void handleTurboMode(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        commandSet.setPowerState(true);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setTurboMode(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setTurboMode(true);
        } else {
            logger.debug("Unknown turbo mode command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    public void handleScreenDisplay(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command.equals(OnOffType.OFF)) {
            commandSet.setScreenDisplay(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setScreenDisplay(true);
        } else {
            logger.debug("Unknown screen display command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    public void handleTempUnit(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command.equals(OnOffType.OFF)) {
            commandSet.setFahrenheit(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setFahrenheit(true);
        } else {
            logger.debug("Unknown temperature unit/farenheit command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    @Override
    public void initialize() {
        connectionManager.disconnect();

        config = getConfigAs(MideaACConfiguration.class);
        logger.debug("MideaACHandler config for {} is {}", thing.getUID(), config);

        if (!config.isValid()) {
            logger.warn("Configuration invalid for {}", thing.getUID());
            if (config.isDiscoveryNeeded()) {
                logger.warn("Discovery needed, discovering....", thing.getUID());
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Configuration missing, discovery needed. Discovering...");
                MideaACDiscoveryService discoveryService = new MideaACDiscoveryService();
                try {
                    discoveryService.discoverThing(config.getIpAddress(), this);
                } catch (Exception e) {
                    logger.error("Discovery failure for {}: {}", thing.getUID(), e.getMessage());
                }
                return;
            } else {
                logger.debug("MideaACHandler config of {} is invalid. Check configuration", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid MideaAC config. Check configuration.");
                return;
            }
        } else {
            logger.debug("Configuration valid for {}", thing.getUID());
        }

        ipAddress = config.getIpAddress();
        ipPort = config.getIpPort();
        deviceId = config.getDeviceId();

        logger.debug("IPAddress: {}", ipAddress);
        logger.debug("IPPort: {}", ipPort);
        logger.debug("ID: {}", deviceId);

        updateStatus(ThingStatus.UNKNOWN);

        connectionManager.connect();
    }

    @Override
    public void discovered(DiscoveryResult discoveryResult) {
        logger.debug("Discovered {}", thing.getUID());
        String deviceId = discoveryResult.getProperties().get(CONFIG_DEVICEID).toString();
        String ipPort = discoveryResult.getProperties().get(CONFIG_IP_PORT).toString();

        Configuration configuration = editConfiguration();
        configuration.put(CONFIG_DEVICEID, deviceId);
        configuration.put(CONFIG_IP_PORT, ipPort);
        updateConfiguration(configuration);

        initialize();
    }

    /*
     * Manage the ONLINE/OFFLINE status of the thing
     */
    private void markOnline() {
        if (!isOnline()) {
            logger.debug("Changing status of {} from {}({}) to ONLINE", thing.getUID(), getStatus(), getDetail());
            updateStatus(ThingStatus.ONLINE);
            // logger.debug(Arrays.toString(Thread.currentThread().getStackTrace()).replace(',', '\n'));

        }
    }

    private void markOffline() {
        if (isOnline()) {
            logger.debug("Changing status of {} from {}({}) to OFFLINE", thing.getUID(), getStatus(), getDetail());
            updateStatus(ThingStatus.OFFLINE);
            // logger.debug(Arrays.toString(Thread.currentThread().getStackTrace()).replace(',', '\n'));
        }
    }

    private void markOfflineWithMessage(ThingStatusDetail statusDetail, String statusMessage) {
        // If it's offline with no detail or if it's not offline, mark it offline with detailed status
        if ((isOffline() && getDetail() == ThingStatusDetail.NONE)
                || (isOffline() && !statusMessage.equals(getDescription())) || !isOffline()) {
            logger.debug("Changing status of {} from {}({}) to OFFLINE({})", thing.getUID(), getStatus(), getDetail(),
                    statusDetail);
            if (isOffline()) {
                updateStatus(ThingStatus.UNKNOWN);
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }

            updateStatus(ThingStatus.OFFLINE, statusDetail, statusMessage);
            // logger.debug(Arrays.toString(Thread.currentThread().getStackTrace()).replace(',', '\n'));
            return;
        }
    }

    private boolean isOnline() {
        return thing.getStatus().equals(ThingStatus.ONLINE);
    }

    private boolean isOffline() {
        return thing.getStatus().equals(ThingStatus.OFFLINE);
    }

    private ThingStatus getStatus() {
        return thing.getStatus();
    }

    private ThingStatusDetail getDetail() {
        return thing.getStatusInfo().getStatusDetail();
    }

    private String getDescription() {
        return thing.getStatusInfo().getDescription();
    }

    /*
     * The {@link ConnectionManager} class is responsible for managing the state of the TCP connection to the
     * fan.
     *
     * @author Jacek Dobrowolski
     */
    private class ConnectionManager {
        private Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

        private boolean deviceIsConnected;

        private InetAddress ifAddress;
        private Socket socket;
        private InputStream inputStream;
        private DataOutputStream writer;
        private final int SOCKET_CONNECT_TIMEOUT = 4000;

        ScheduledFuture<?> connectionMonitorJob;
        private final long CONNECTION_MONITOR_FREQ = 10L;
        private final long CONNECTION_MONITOR_DELAY = 10L;

        private Response lastResponse;

        public Response getLastResponse() {
            return lastResponse;
        }

        Runnable connectionMonitorRunnable = () -> {
            logger.trace("Performing connection check for {} at IP {}", thing.getUID(), ipAddress);
            checkConnection();
        };

        public ConnectionManager(String ipv4Address) {
            deviceIsConnected = false;
        }

        /*
         * Connect to the command and serial port(s) on the device. The serial connections are established only for
         * devices that support serial.
         */
        protected synchronized void connect() {
            if (isConnected()) {
                return;
            }
            logger.trace("Connecting to {} at {}:{}", thing.getUID(), ipAddress, ipPort);

            // Open socket
            try {
                socket = new Socket();
                socket.setSoTimeout(SOCKET_CONNECT_TIMEOUT);
                socket.bind(new InetSocketAddress(0)); // TODO: allow choosing adapter?
                socket.connect(new InetSocketAddress(ipAddress, Integer.valueOf(ipPort)), SOCKET_CONNECT_TIMEOUT);
            } catch (IOException e) {
                logger.debug("IOException connecting to  {} at {}: {}", thing.getUID(), ipAddress, e.getMessage());
                markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                disconnect();
                scheduleConnectionMonitorJob();
                return;
            }

            // Create streams
            try {
                writer = new DataOutputStream(socket.getOutputStream());
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                logger.warn("IOException getting streams for {} at {}: {}", thing.getUID(), ipAddress, e.getMessage(),
                        e);
                markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                disconnect();
                return;
            }
            logger.info("Connected to {} at {}", thing.getUID(), ipAddress);
            deviceIsConnected = true;
            markOnline();

            requestStatus(true);
        }

        public void requestStatus(boolean restartMonitor) {
            CommandBase requestStatusCommand = new CommandBase();
            if (restartMonitor) {
                sendCommandAndMonitor(requestStatusCommand);
            } else {
                sendCommand(requestStatusCommand);
            }
        }

        public void sendCommandAndMonitor(CommandBase command) {
            cancelConnectionMonitorJob();
            sendCommand(command);
            scheduleConnectionMonitorJob();
        }

        public void sendCommand(CommandBase command) {
            if (command instanceof CommandSet) {
                ((CommandSet) command).setPromptTone(config.getPromptTone());
            }
            Packet packet = new Packet(command, deviceId);
            packet.finalize();

            if (!isConnected()) {
                logger.debug("Unable to send message; no connection to {}. Trying to reconnect: {}", thing.getUID(),
                        command);
                connect();
                if (isConnected()) {
                    sendCommand(command);
                    return;
                }
            }

            try {
                byte[] bytes = packet.getBytes();
                logger.debug("Writing to {} at {} bytes.length: {}, bytes: {}", thing.getUID(), ipAddress, bytes.length,
                        Utils.bytesToHex(bytes));
                write(bytes);

                byte[] responseBytes = read();

                if (responseBytes != null) {
                    markOnline();
                    lastResponse = new Response(responseBytes);
                    processMessage(lastResponse);
                    return;
                }

            } catch (SocketException e) {
                logger.debug("SocketException writing to  {} at {}: {}", thing.getUID(), ipAddress, e.getMessage());
                markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                disconnect();
            } catch (IOException e) {
                logger.debug("IOException writing to  {} at {}: {}", thing.getUID(), ipAddress, e.getMessage());
                markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                disconnect();
            }
            scheduleConnectionMonitorJob();
        }

        protected synchronized void disconnect() {
            if (!isConnected()) {
                return;
            }
            logger.debug("Disconnecting from {} at {}", thing.getUID(), ipAddress);

            try {
                if (writer != null) {
                    writer.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                logger.warn("IOException closing connection to {} at {}: {}", thing.getUID(), ipAddress, e.getMessage(),
                        e);
            }
            deviceIsConnected = false;
            socket = null;
            inputStream = null;
            writer = null;
            markOffline();
        }

        private void updateChannel(String channelName, State state) {
            Channel channel = thing.getChannel(channelName);
            if (channel != null) {
                updateState(channel.getUID(), state);
            }
        }

        private void processMessage(Response response) {

            updateChannel(CHANNEL_POWER, response.getPowerState() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_IMODE_RESUME, response.getImmodeResume() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_TIMER_MODE, response.getTimerMode() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_APPLIANCE_ERROR, response.getApplianceError() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_TARGET_TEMPERATURE, new DecimalType(response.getTargetTemperature()));
            updateChannel(CHANNEL_OPERATIONAL_MODE, new StringType(response.getOperationalMode().toString()));
            updateChannel(CHANNEL_FAN_SPEED, new StringType(response.getFanSpeed().name()));
            updateChannel(CHANNEL_ON_TIMER, new StringType(response.getOnTimer().toChannel()));
            updateChannel(CHANNEL_OFF_TIMER, new StringType(response.getOffTimer().toChannel()));
            updateChannel(CHANNEL_SWING_MODE, new StringType(response.getSwingMode().toString()));
            updateChannel(CHANNEL_COZY_SLEEP, new DecimalType(response.getCozySleep()));
            updateChannel(CHANNEL_SAVE, response.getSave() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_LOW_FREQUENCY_FAN,
                    response.getLowFrequencyFan() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_SUPER_FAN, response.getSuperFan() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_FEEL_OWN, response.getFeelOwn() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_CHILD_SLEEP_MODE,
                    response.getChildSleepMode() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_EXCHANGE_AIR, response.getExchangeAir() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_DRY_CLEAN, response.getDryClean() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_AUX_HEAT, response.getAuxHeat() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_ECO_MODE, response.getEcoMode() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_CLEAN_UP, response.getCleanUp() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_TEMP_UNIT, response.getTempUnit() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_SLEEP_FUNCTION, response.getSleepFunction() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_TURBO_MODE, response.getTurboMode() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_CATCH_COLD, response.getCatchCold() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_NIGHT_LIGHT, response.getNightLight() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_PEAK_ELEC, response.getPeakElec() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_NATURAL_FAN, response.getNaturalFan() == true ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_INDOOR_TEMPERATURE, new DecimalType(response.getIndoorTemperature()));
            updateChannel(CHANNEL_OUTDOOR_TEMPERATURE, new DecimalType(response.getOutdoorTemperature()));
            updateChannel(CHANNEL_HUMIDITY, new DecimalType(response.getHumidity()));
        }

        public byte[] read() {
            byte[] bytes = new byte[512];
            try {
                int len = inputStream.read(bytes);
                if (len > 0) {
                    logger.debug("Response received length: {}", len);
                    bytes = Arrays.copyOfRange(bytes, 0, len);
                    logger.debug("Response bytes: {}", Utils.bytesToHex(bytes));
                    return bytes;
                }
            } catch (IOException e) {
                disconnect();
                markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }

            return null;
        }

        public void write(byte[] buffer) throws IOException {
            if (writer == null) {
                logger.warn("fanWriter for {} is null when trying to write to {}!!!", thing.getUID(), ipAddress);
                return;
            }
            writer.write(buffer, 0, buffer.length);
        }

        private boolean isConnected() {
            return deviceIsConnected && !socket.isClosed() && socket.isConnected();
        }

        /*
         * Periodically validate the command connection to the device by executing a getversion command.
         */
        private void scheduleConnectionMonitorJob() {
            if (connectionMonitorJob == null) {
                logger.debug("Starting connection monitor job in {} seconds for {} at {}", config.getPollingTime(),
                        thing.getUID(), ipAddress);
                connectionMonitorJob = scheduler.scheduleWithFixedDelay(connectionMonitorRunnable,
                        CONNECTION_MONITOR_DELAY, CONNECTION_MONITOR_FREQ, TimeUnit.SECONDS);
            }
        }

        private void cancelConnectionMonitorJob() {
            if (connectionMonitorJob != null) {
                logger.debug("Cancelling connection monitor job for {} at {}", thing.getUID(), ipAddress);
                connectionMonitorJob.cancel(true);
                connectionMonitorJob = null;
            }
        }

        private void checkConnection() {
            logger.trace("Checking status of connection for {} at {}", thing.getUID(), ipAddress);
            if (!isConnected()) {
                logger.debug("Connection check FAILED for {} at {}", thing.getUID(), ipAddress);
                connect();
            } else {
                logger.debug("Connection check OK for {} at {}", thing.getUID(), ipAddress);
                logger.debug("Requesting status update from {} at {}", thing.getUID(), ipAddress);
                requestStatus(false);
            }
        }

        public void dispose() {
            cancelConnectionMonitorJob();
        }
    }

}
