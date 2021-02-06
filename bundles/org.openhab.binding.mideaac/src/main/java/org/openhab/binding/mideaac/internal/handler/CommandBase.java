package org.openhab.binding.mideaac.internal.handler;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
import org.openhab.binding.mideaac.internal.security.Crc8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for Midea AC.
 *
 * @author Jacek Dobrowolski
 */
public class CommandBase {
    private static Logger logger = LoggerFactory.getLogger(CommandBase.class);

    private static final byte[] DISCOVER_COMMAND = { (byte) 0x5a, (byte) 0x5a, (byte) 0x01, (byte) 0x11, (byte) 0x48,
            (byte) 0x00, (byte) 0x92, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0x75, (byte) 0xbd, (byte) 0x6b, (byte) 0x3e,
            (byte) 0x4f, (byte) 0x8b, (byte) 0x76, (byte) 0x2e, (byte) 0x84, (byte) 0x9c, (byte) 0x6e, (byte) 0x57,
            (byte) 0x8d, (byte) 0x65, (byte) 0x90, (byte) 0x03, (byte) 0x6e, (byte) 0x9d, (byte) 0x43, (byte) 0x42,
            (byte) 0xa5, (byte) 0x0f, (byte) 0x1f, (byte) 0x56, (byte) 0x9e, (byte) 0xb8, (byte) 0xec, (byte) 0x91,
            (byte) 0x8e, (byte) 0x92, (byte) 0xe5 };

    protected byte[] data;

    public enum OperationalMode {
        AUTO(1),
        COOL(2),
        DRY(3),
        HEAT(4),
        FAN_ONLY(5);

        private final int value;

        private OperationalMode(int value) {
            this.value = value;
        }

        public int getId() {
            return value;
        }

        public static OperationalMode fromId(int id) {
            for (OperationalMode type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return null;
        }
    }

    public enum SwingMode {
        OFF(0),
        VERTICAL(0xC),
        HORIZONTAL(0x3),
        BOTH(0xF);

        private final int value;

        private SwingMode(int value) {
            this.value = value;
        }

        public int getId() {
            return value;
        }

        public static SwingMode fromId(int id) {
            for (SwingMode type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return null;
        }
    }

    public enum FanSpeed {
        AUTO(102),
        HIGH(80),
        MEDIUM(60),
        LOW(40),
        SILENT(20);

        private final int value;

        private FanSpeed(int value) {
            this.value = value;
        }

        public int getId() {
            return value;
        }

        public static FanSpeed fromId(int id) {
            for (FanSpeed type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * Returns the command to discover devices.
     *
     * @return discover command
     */
    public static byte[] discover() {
        return DISCOVER_COMMAND;
    }

    public CommandBase() {
        data = new byte[] { (byte) 0xaa,
                // request is 0x20; setting is 0x23
                (byte) 0x20,
                // device type
                (byte) 0xac, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // request is 0x03; setting is 0x02
                (byte) 0x03,
                // Byte0 - Data request/response type: 0x41 - check status; 0x40 - Set up
                (byte) 0x41,
                // Byte1
                (byte) 0x81,
                // Byte2 - operational_mode
                0x00,
                // Byte3
                (byte) 0xff,
                // Byte4
                0x03,
                // Byte5
                (byte) 0xff,
                // Byte6
                0x00,
                // Byte7 - Room Temperature Request: 0x02 - indoor_temperature, 0x03 - outdoor_temperature
                // when set, this is swing_mode
                0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // Message ID
                0x00 };
        Date d = new Date();
        data[data.length - 1] = (byte) d.getSeconds();
        data[0x02] = (byte) 0xAC;
    }

    @Override
    public void finalize() {
        byte crc8 = (byte) Crc8.calculate(Arrays.copyOfRange(data, 10, data.length));
        data = ArrayUtils.add(data, crc8);

        byte chksum = checksum(Arrays.copyOfRange(data, 1, data.length));
        data = ArrayUtils.add(data, chksum);
    }

    public byte[] getBytes() {
        return data;
    }

    private static byte checksum(byte[] bytes) {
        int sum = 0;
        for (byte value : bytes) {
            sum = (byte) (sum + value);
        }
        sum = (byte) ((255 - (sum % 256)) + 1);
        return (byte) sum;
    }

}
