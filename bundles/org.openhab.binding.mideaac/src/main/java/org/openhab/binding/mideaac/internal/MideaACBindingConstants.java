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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MideaACBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jacek Dobrowolski
 */
@NonNullByDefault
public class MideaACBindingConstants {

    private static final String BINDING_ID = "mideaac";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MIDEAAC = new ThingTypeUID(BINDING_ID, "ac");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MIDEAAC);

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_IMODE_RESUME = "imodeResume";
    public static final String CHANNEL_TIMER_MODE = "timerMode";
    public static final String CHANNEL_APPLIANCE_ERROR = "applianceError";
    public static final String CHANNEL_TARGET_TEMPERATURE = "targetTemperature";
    public static final String CHANNEL_OPERATIONAL_MODE = "operationalMode";
    public static final String CHANNEL_FAN_SPEED = "fanSpeed";
    public static final String CHANNEL_ON_TIMER = "onTimer";
    public static final String CHANNEL_OFF_TIMER = "offTimer";
    public static final String CHANNEL_SWING_MODE = "swingMode";
    public static final String CHANNEL_COZY_SLEEP = "cozySleep";
    public static final String CHANNEL_SAVE = "save";
    public static final String CHANNEL_LOW_FREQUENCY_FAN = "lowFrequencyFan";
    public static final String CHANNEL_SUPER_FAN = "superFan";
    public static final String CHANNEL_FEEL_OWN = "feelOwn";
    public static final String CHANNEL_CHILD_SLEEP_MODE = "childSleepMode";
    public static final String CHANNEL_EXCHANGE_AIR = "exchangeAir";
    public static final String CHANNEL_DRY_CLEAN = "dryClean";
    public static final String CHANNEL_AUX_HEAT = "auxHeat";
    public static final String CHANNEL_ECO_MODE = "ecoMode";
    public static final String CHANNEL_CLEAN_UP = "cleanUp";
    public static final String CHANNEL_TEMP_UNIT = "tempUnit";
    public static final String CHANNEL_SLEEP_FUNCTION = "sleepFunction";
    public static final String CHANNEL_TURBO_MODE = "turboMode";
    public static final String CHANNEL_CATCH_COLD = "catchCold";
    public static final String CHANNEL_NIGHT_LIGHT = "nightLight";
    public static final String CHANNEL_PEAK_ELEC = "peakElec";
    public static final String CHANNEL_NATURAL_FAN = "naturalFan";
    public static final String CHANNEL_INDOOR_TEMPERATURE = "indoorTemperature";
    public static final String CHANNEL_OUTDOOR_TEMPERATURE = "outdoorTemperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_SCREEN_DISPLAY = "screenDisplay";

    public static final Unit<Temperature> API_TEMPERATURE_UNIT = SIUnits.CELSIUS;

    public static final Set<String> SUPPORTED_CHANNEL_IDS = Stream
            .of(CHANNEL_POWER, CHANNEL_TARGET_TEMPERATURE, CHANNEL_INDOOR_TEMPERATURE, CHANNEL_OUTDOOR_TEMPERATURE)
            .collect(Collectors.toSet());

    // Commands sent to/from fan are ASCII
    public static final String CHARSET = "US-ASCII";

    // List of al property ids
    public static final String CONFIG_IP_ADDRESS = "ipAddress";
    public static final String CONFIG_IP_PORT = "ipPort";
    public static final String CONFIG_DEVICEID = "deviceId";
    public static final String CONFIG_POLLING_TIME = "pollingTime";
    public static final String CONFIG_PROMPT_TONE = "promptTone";

    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_SN = "sn";
    public static final String PROPERTY_SSID = "ssid";
    public static final String PROPERTY_TYPE = "type";
}
