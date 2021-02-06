package org.openhab.binding.mideaac.internal.handler;

import org.apache.commons.lang.ArrayUtils;

/**
 * Command changing a Midea AC.
 *
 * @author Jacek Dobrowolski
 */
public class CommandSet extends CommandBase {

    public CommandSet() {
        data[0x01] = (byte) 0x23;
        data[0x09] = (byte) 0x02;
        // Set up Mode
        data[0x0a] = (byte) 0x40;

        byte[] extra = { 0x00, 0x00, 0x00 };
        data = ArrayUtils.addAll(data, extra);
    }

    public static CommandSet fromResponse(Response response) {
        CommandSet commandSet = new CommandSet();

        commandSet.setPowerState(response.getPowerState());
        commandSet.setTargetTemperature(response.getTargetTemperature());
        commandSet.setOperationalMode(response.getOperationalMode());
        commandSet.setFanSpeed(response.getFanSpeed());

        return commandSet;
    }

    public void setPromptTone(boolean feedbackEnabled) {
        if (!feedbackEnabled) {
            data[0x0b] &= ~(byte) 0x42; // Clear the audible bits
        } else {
            data[0x0b] |= (byte) 0x42;
        }
    }

    public void setPowerState(boolean state) {
        if (!state) {
            data[0x0b] &= ~0x01;// Clear the power bit
        } else {
            data[0x0b] |= 0x01;
        }
    }

    public void setOperationalMode(OperationalMode mode) {
        data[0x0c] &= ~(byte) 0xe0; // Clear the mode bit
        data[0x0c] |= ((byte) mode.getId() << 5) & (byte) 0xe0;
    }

    public void setTargetTemperature(float temperature) {
        // Clear the temperature bits.
        data[0x0c] &= ~0x0f;
        // Clear the temperature bits, except the 0.5 bit, which will be set properly in all cases
        data[0x0c] |= (int) temperature & 0xf;
        // set the +0.5 bit
        setTemperatureDot5((Math.round(temperature * 2)) % 2 != 0);
    }

    public void setFanSpeed(FanSpeed speed) {
        setFanSpeed(speed.getId());
    }

    public void setFanSpeed(int speed) {
        data[0x0d] = (byte) speed;
    }

    public void setEcoMode(boolean ecoModeEnabled) {
        data[0x13] = ecoModeEnabled ? (byte) 0xff : 0x00;
    }

    public void setSwingMode(SwingMode mode) {
        data[0x11] = 0x30; // Clear the mode bit
        data[0x11] |= mode.getId() & (byte) 0x3f;
    }

    public void setTurboMode(boolean turboModeEnabled) {
        if (turboModeEnabled) {
            data[0x14] |= 0x02;
        } else {
            data[0x14] &= (~0x02);
        }
    }

    public void setScreenDisplay(boolean screenDisplayEnabed) {
        // the LED lights on the AC. these display temperature and are often too bright during nights
        if (screenDisplayEnabed) {
            data[0x14] |= 0x10;
        } else {
            data[0x14] &= (~0x10);
        }
    }

    private void setTemperatureDot5(boolean temperatureDot5Enabled) {
        // add 0.5C to the temperature value. not intended to be called directly. target_temperature setter calls this
        // if needed
        if (temperatureDot5Enabled) {
            data[0x0c] |= 0x10;
        } else {
            data[0x0c] &= (~0x10);
        }
    }

    public void setFahrenheit(boolean fahrenheitEnabled) {
        // set the unit to fahrenheit from celcius
        if (fahrenheitEnabled) {
            data[0x14] |= 0x04;
        } else {
            data[0x14] &= (~0x04);
        }
    }
}
