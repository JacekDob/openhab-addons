package org.openhab.binding.mideaac.internal.handler;

import java.util.Arrays;

import org.openhab.binding.mideaac.internal.Utils;
import org.openhab.binding.mideaac.internal.handler.CommandBase.FanSpeed;
import org.openhab.binding.mideaac.internal.handler.CommandBase.OperationalMode;
import org.openhab.binding.mideaac.internal.handler.CommandBase.SwingMode;
import org.openhab.binding.mideaac.internal.security.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Response from a device.
 *
 * @author Jacek Dobrowolski
 */
public class Response {
    byte[] data;
    private Logger logger = LoggerFactory.getLogger(Response.class);

    public Response(byte[] rawBytes) {
        data = Security.aes_decrypt(Arrays.copyOfRange(rawBytes, 40, rawBytes.length - 16));

        // The response data from the appliance includes a packet header which we don't want
        data = Arrays.copyOfRange(data, 10, data.length - 1);

        logger.trace("Bytes decoded and stripped without header: {}", Utils.bytesToHex(data));

        logger.trace("PowerState: {}", getPowerState());
        logger.trace("ImodeResume: {}", getImmodeResume());
        logger.trace("TimerMode: {}", getTimerMode());
        logger.trace("ApplianceError: {}", getApplianceError());
        logger.trace("TargetTemperature: {}", getTargetTemperature());
        logger.trace("OperationalMode: {}", getOperationalMode());
        logger.trace("FanSpeed: {}", getFanSpeed());
        logger.trace("OnTimer: {}", getOnTimer());
        logger.trace("OffTimer: {}", getOffTimer());
        logger.trace("SwingMode: {}", getSwingMode());
        logger.trace("CozySleep: {}", getCozySleep());
        logger.trace("Save: {}", getSave());
        logger.trace("LowFrequencyFan: {}", getLowFrequencyFan());
        logger.trace("SuperFan: {}", getSuperFan());
        logger.trace("FeelOwn: {}", getFeelOwn());
        logger.trace("ChildSleepMode: {}", getChildSleepMode());
        logger.trace("ExchangeAir: {}", getExchangeAir());
        logger.trace("DryClean: {}", getDryClean());
        logger.trace("AuxHeat: {}", getAuxHeat());
        logger.trace("EcoMode: {}", getEcoMode());
        logger.trace("CleanUp: {}", getCleanUp());
        logger.trace("TempUnit: {}", getTempUnit());
        logger.trace("SleepFunction: {}", getSleepFunction());
        logger.trace("TurboMode: {}", getTurboMode());
        logger.trace("CatchCold: {}", getCatchCold());
        logger.trace("NightLight: {}", getNightLight());
        logger.trace("PeakElec: {}", getPeakElec());
        logger.trace("NaturalFan: {}", getNaturalFan());
        logger.trace("IndoorTemperature: {}", getIndoorTemperature());
        logger.trace("OutdoorTemperature: {}", getOutdoorTemperature());
        logger.trace("Humidity: {}", getHumidity());
    }

    public boolean getPowerState() {
        return (data[0x01] & 0x1) > 0;
    }

    public boolean getImmodeResume() {
        return (data[0x01] & 0x4) > 0;
    }

    public boolean getTimerMode() {
        return (data[0x01] & 0x10) > 0;
    }

    public boolean getApplianceError() {
        return (data[0x01] & 0x80) > 0;
    }

    public float getTargetTemperature() {
        return (data[0x02] & 0xf) + 16.0f + (((data[0x02] & 0x10) > 0) ? 0.5f : 0.0f);
    }

    public OperationalMode getOperationalMode() {
        return OperationalMode.fromId((data[0x02] & 0xe0) >> 5);
    }

    public FanSpeed getFanSpeed() {
        return FanSpeed.fromId(data[0x03] & 0x7f);
    }

    public Timer getOnTimer() {
        int on_timer_value = data[0x04];
        int on_timer_minutes = data[0x06];
        return new Timer(((on_timer_value & (byte) 0x80) >> 7) > 0, (on_timer_value & (byte) 0x7c) >> 2,
                (on_timer_value & 0x3) | ((on_timer_minutes & (byte) 0xf0) >> 4));
    }

    public Timer getOffTimer() {
        int off_timer_value = data[0x05];
        int off_timer_minutes = data[0x06];
        return new Timer(((off_timer_value & (byte) 0x80) >> 7) > 0, (off_timer_value & (byte) 0x7c) >> 2,
                (off_timer_value & 0x3) | (off_timer_minutes & (byte) 0xf));
    }

    public SwingMode getSwingMode() {
        return SwingMode.fromId(data[0x07] & 0x0f);
    }

    public int getCozySleep() {
        return data[0x08] & (byte) 0x03;
    }

    public boolean getSave() {
        return (data[0x08] & (byte) 0x08) != 0;
    }

    public boolean getLowFrequencyFan() {
        return (data[0x08] & (byte) 0x10) != 0;
    }

    public boolean getSuperFan() {
        return (data[0x08] & (byte) 0x20) != 0;
    }

    public boolean getFeelOwn() {
        return (data[0x08] & (byte) 0x80) != 0;
    }

    public boolean getChildSleepMode() {
        return (data[0x09] & (byte) 0x02) != 0;
    }

    public boolean getExchangeAir() {
        return (data[0x09] & (byte) 0x02) != 0;
    }

    public boolean getDryClean() {
        return (data[0x09] & (byte) 0x04) != 0;
    }

    public boolean getAuxHeat() {
        return (data[0x09] & (byte) 0x08) != 0;
    }

    public boolean getEcoMode() {
        return (data[0x09] & (byte) 0x10) != 0;
    }

    public boolean getCleanUp() {
        return (data[0x09] & (byte) 0x20) != 0;
    }

    public boolean getTempUnit() {
        return (data[0x09] & (byte) 0x80) != 0;
    }

    public boolean getSleepFunction() {
        return (data[0x0a] & (byte) 0x01) != 0;
    }

    public boolean getTurboMode() {
        return (data[0x0a] & (byte) 0x02) != 0;
    }

    public boolean getCatchCold() {
        return (data[0x0a] & (byte) 0x08) != 0;
    }

    public boolean getNightLight() {
        return (data[0x0a] & (byte) 0x10) != 0;
    }

    public boolean getPeakElec() {
        return (data[0x0a] & (byte) 0x20) != 0;
    }

    public boolean getNaturalFan() {
        return (data[0x0a] & (byte) 0x40) != 0;
    }

    public Float getIndoorTemperature() {

        int indoorTempInteger;
        float indoorTempDecimal;

        if (data[0] == (byte) 0xc0) {
            if (((Byte.toUnsignedInt(data[11]) - 50) / 2.0f) < -19
                    || ((Byte.toUnsignedInt(data[11]) - 50) / 2.0f) > 50) {
                return null;
            } else {
                indoorTempInteger = ((Byte.toUnsignedInt(data[11]) - 50) / 2);
            }

            int indoorTemperatureDot = getBits(data, 15, 0, 3);

            indoorTempDecimal = indoorTemperatureDot * 0.1f;

            if (data[11] > 49) {
                return indoorTempInteger + indoorTempDecimal;
            } else {
                return indoorTempInteger - indoorTempDecimal;
            }
        }
        if (data[0] == (byte) 0xa0 || data[0] == (byte) 0xa1) {
            if (data[0] == (byte) 0xa0) {
                if ((data[1] >> 2) - 4 == 0) {
                    indoorTempInteger = -1;
                } else {
                    indoorTempInteger = (data[1] >> 2) + 12;
                }

                if (((data[1] >> 1) & 0x01) == 1) {
                    indoorTempDecimal = 0.5f;
                } else {
                    indoorTempDecimal = 0;
                }
            }
            if (data[0] == (byte) 0xa1) {
                if (((Byte.toUnsignedInt(data[13]) - 50) / 2) < -19 || ((Byte.toUnsignedInt(data[13]) - 50) / 2) > 50) {
                    return null;
                } else {
                    indoorTempInteger = (Byte.toUnsignedInt(data[13]) - 50) / 2;
                }
                indoorTempDecimal = (data[18] & 0x0f) * 0.1f;

                if (Byte.toUnsignedInt(data[13]) > 49) {
                    return indoorTempInteger + indoorTempDecimal;
                } else {
                    return indoorTempInteger - indoorTempDecimal;
                }
            }
        }

        return null;
    }

    public float getOutdoorTemperature() {
        return (Byte.toUnsignedInt(data[0x0c]) - 50) / 2.0f;
    }

    public int getHumidity() {
        return (data[0x0d] & (byte) 0x7f);
    }

    private static int getBit(byte pByte, int pIndex) {
        return (pByte >> pIndex) & (byte) 0x01;
    }

    private static int getBits(byte[] pBytes, int pIndex, int pStartIndex, int pEndIndex) {
        int StartIndex, EndIndex;

        if (pStartIndex > pEndIndex) {
            StartIndex = pEndIndex;
            EndIndex = pStartIndex;
        } else {
            StartIndex = pStartIndex;
            EndIndex = pEndIndex;
        }

        int tempVal = 0x00;

        int i = StartIndex;

        while (i <= EndIndex) {
            tempVal = tempVal | getBit(pBytes[pIndex], i) << (i - StartIndex);
            i += 1;
        }
        return tempVal;
    }
}
