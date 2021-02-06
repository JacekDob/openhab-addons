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
    public static final String CHANNEL_IMODE_RESUME = "imode-resume";
    public static final String CHANNEL_TIMER_MODE = "timer-mode";
    public static final String CHANNEL_APPLIANCE_ERROR = "appliance-error";
    public static final String CHANNEL_TARGET_TEMPERATURE = "target-temperature";
    public static final String CHANNEL_OPERATIONAL_MODE = "operational-mode";
    public static final String CHANNEL_FAN_SPEED = "fan-speed";
    public static final String CHANNEL_ON_TIMER = "on-timer";
    public static final String CHANNEL_OFF_TIMER = "off-timer";
    public static final String CHANNEL_SWING_MODE = "swing-mode";
    public static final String CHANNEL_COZY_SLEEP = "cozy-sleep";
    public static final String CHANNEL_SAVE = "save";
    public static final String CHANNEL_LOW_FREQUENCY_FAN = "low-frequency-fan";
    public static final String CHANNEL_SUPER_FAN = "super-fan";
    public static final String CHANNEL_FEEL_OWN = "feel-own";
    public static final String CHANNEL_CHILD_SLEEP_MODE = "child-sleep-mode";
    public static final String CHANNEL_EXCHANGE_AIR = "exchange-air";
    public static final String CHANNEL_DRY_CLEAN = "dry-clean";
    public static final String CHANNEL_AUX_HEAT = "aux-heat";
    public static final String CHANNEL_ECO_MODE = "eco-mode";
    public static final String CHANNEL_CLEAN_UP = "clean-up";
    public static final String CHANNEL_TEMP_UNIT = "temp-unit";
    public static final String CHANNEL_SLEEP_FUNCTION = "sleep-function";
    public static final String CHANNEL_TURBO_MODE = "turbo-mode";
    public static final String CHANNEL_CATCH_COLD = "catch-cold";
    public static final String CHANNEL_NIGHT_LIGHT = "night-light";
    public static final String CHANNEL_PEAK_ELEC = "peak-elec";
    public static final String CHANNEL_NATURAL_FAN = "natural-fan";
    public static final String CHANNEL_INDOOR_TEMPERATURE = "indoor-temperature";
    public static final String CHANNEL_OUTDOOR_TEMPERATURE = "outdoor-temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_PROMPT_TONE = "prompt-tone";
    public static final String CHANNEL_SCREEN_DISPLAY = "screen-display";

    public static final Unit<Temperature> API_TEMPERATURE_UNIT = SIUnits.CELSIUS;

    public static final Set<String> SUPPORTED_CHANNEL_IDS = Stream
            .of(CHANNEL_POWER, CHANNEL_TARGET_TEMPERATURE, CHANNEL_INDOOR_TEMPERATURE, CHANNEL_OUTDOOR_TEMPERATURE)
            .collect(Collectors.toSet());

    // Commands sent to/from fan are ASCII
    public static final String CHARSET = "US-ASCII";

    // List of al property ids
    public static final String CONFIG_IP = "ipAddress";
    public static final String CONFIG_DEVICEID = "deviceId";
    public static final String CONFIG_PORT = "ipPort";
    public static final String CONFIG_POLLING_TIME = "pollingTime";
    public static final String CONFIG_PROMPT_TONE = "pomptTone";

    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_SN = "sn";
    public static final String PROPERTY_SSID = "ssid";
    public static final String PROPERTY_TYPE = "type";
}
