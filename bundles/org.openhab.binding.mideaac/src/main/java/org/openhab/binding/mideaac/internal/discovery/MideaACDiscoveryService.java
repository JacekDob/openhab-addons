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
package org.openhab.binding.mideaac.internal.discovery;

import static org.openhab.binding.mideaac.internal.MideaACBindingConstants.*;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
//import org.bouncycastle.util.Arrays;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mideaac.internal.Utils;
import org.openhab.binding.mideaac.internal.handler.CommandBase;
import org.openhab.binding.mideaac.internal.security.Security;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for Midea AC.
 *
 * @author Jacek Dobrowolski
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.mideaac")
public class MideaACDiscoveryService extends AbstractDiscoveryService {

    private static final int DISCOVERY_TIMEOUT_SECONDS = 5; // 5;
    private static final int RECEIVE_JOB_TIMEOUT = 20000;
    private static final int UDP_PACKET_TIMEOUT = RECEIVE_JOB_TIMEOUT - 50;
    private static final String MIDEAAC_NAME_PREFIX = "MideaAC";

    private final Logger logger = LoggerFactory.getLogger(MideaACDiscoveryService.class);

    ///// Network
    private final byte[] buffer = new byte[512];
    @Nullable
    private DatagramSocket discoverSocket;

    public MideaACDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {
        logger.debug("Start scan for Midea AC devices.");
        discoverThings();
    }

    @Override
    protected void stopScan() {
        logger.debug("Stop scan for Midea AC devices.");
        closeDiscoverSocket();
        super.stopScan();
    }

    /**
     * Performs the actual discovery of Midea AC devices (things).
     */
    private void discoverThings() {
        try {
            final DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            // No need to call close first, because the caller of this method already has done it.
            startDiscoverSocket();
            // Runs until the socket call gets a time out and throws an exception. When a time out is triggered it means
            // no data was present and nothing new to discover.
            while (true) {
                // Set packet length in case a previous call reduced the size.
                receivePacket.setLength(buffer.length);
                if (discoverSocket == null) {
                    break;
                } else {
                    discoverSocket.receive(receivePacket);
                }
                logger.debug("Midea AC device discovery returned package with length {}", receivePacket.getLength());
                if (receivePacket.getLength() > 0) {
                    thingDiscovered(receivePacket);
                }
            }
        } catch (SocketTimeoutException e) {
            logger.debug("Discovering poller timeout...");
        } catch (IOException e) {
            // logger.debug("Error during discovery: {}", e.getMessage());
        } finally {
            closeDiscoverSocket();
            removeOlderResults(getTimestampOfLastScan());
        }
    }

    /**
     * Opens a {@link DatagramSocket} and sends a packet for discovery of Midea AC devices.
     *
     * @throws SocketException
     * @throws IOException
     */
    private void startDiscoverSocket() throws SocketException, IOException {
        discoverSocket = new DatagramSocket(new InetSocketAddress(Connection.MIDEAAC_RECEIVE_PORT));
        discoverSocket.setBroadcast(true);
        discoverSocket.setSoTimeout(UDP_PACKET_TIMEOUT);
        final InetAddress broadcast = InetAddress.getByName("255.255.255.255");
        {
            final DatagramPacket discoverPacket = new DatagramPacket(CommandBase.discover(),
                    CommandBase.discover().length, broadcast, Connection.MIDEAAC_SEND_PORT1);
            discoverSocket.send(discoverPacket);
            if (logger.isTraceEnabled()) {
                logger.trace("Broadcast discovery package sent to port: {}", Connection.MIDEAAC_SEND_PORT1);
            }
        }
        {
            final DatagramPacket discoverPacket = new DatagramPacket(CommandBase.discover(),
                    CommandBase.discover().length, broadcast, Connection.MIDEAAC_SEND_PORT2);
            discoverSocket.send(discoverPacket);
            if (logger.isTraceEnabled()) {
                logger.trace("Broadcast discovery package sent to port: {}", Connection.MIDEAAC_SEND_PORT2);
            }
        }
    }

    /**
     * Closes the discovery socket and cleans the value. No need for synchronization as this method is called from a
     * synchronized context.
     */
    private void closeDiscoverSocket() {
        if (discoverSocket != null) {
            discoverSocket.close();
            discoverSocket = null;
        }
    }

    /**
     * Register a device (thing) with the discovered properties.
     *
     * @param packet containing data of detected device
     */
    private void thingDiscovered(DatagramPacket packet) {
        final String ipAddress = packet.getAddress().getHostAddress();
        byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());

        logger.debug("Data.Length: '{}' from {}", data.length, ipAddress);

        if (data.length != 120) {
            logger.debug(
                    "Midea AC device was detected, but the retrieved data is incomplete: '{}'. Device not registered",
                    new String(packet.getData(), 0, packet.getLength() - 1, StandardCharsets.UTF_8));
        } else {
            logger.debug("Midea AC discover data: '{}'",
                    // new String(packet.getData(), 0, packet.getLength() - 1, StandardCharsets.UTF_8),
                    Utils.bytesToHex(data));

            if (data.length >= 104 && (Utils.bytesToHex(Arrays.copyOfRange(data, 0, 2)).equals("5A5A")
                    || Utils.bytesToHex(Arrays.copyOfRange(data, 8, 10)).equals("5A5A"))) {
                logger.trace("Device supported");
                String m_id, m_version = "", m_ip = "", m_port = "", m_sn = "", m_ssid = "", m_type = "";
                if (Utils.bytesToHex(Arrays.copyOfRange(data, 0, 2)).equals("5A5A")) {
                    m_version = "2";
                }
                if (Utils.bytesToHex(Arrays.copyOfRange(data, 0, 2)).equals("8370")) {
                    m_version = "3";
                }
                if (Utils.bytesToHex(Arrays.copyOfRange(data, 8, 10)).equals("5a5a")) {
                    data = Arrays.copyOfRange(data, 8, data.length - 8);
                }

                logger.trace("Version: {}", m_version);

                byte[] id = Arrays.copyOfRange(data, 20, 26);
                logger.trace("Id Bytes: {}", Utils.bytesToHex(id));

                ArrayUtils.reverse(id);
                BigInteger bi_id = new BigInteger(id);
                m_id = bi_id.toString();

                logger.debug("Id: '{}'", m_id);

                byte[] encrypt_data = Arrays.copyOfRange(data, 40, data.length - 16);
                logger.debug("Encrypt data: '{}'", Utils.bytesToHex(encrypt_data));

                byte[] reply = Security.aes_decrypt(encrypt_data);
                logger.debug("Length: {}, Reply: '{}'", reply.length, Utils.bytesToHex(reply));
                logger.debug("Reply: '{}'", new String(reply, StandardCharsets.UTF_8));

                m_ip = Byte.toUnsignedInt(reply[3]) + "." + Byte.toUnsignedInt(reply[2]) + "."
                        + Byte.toUnsignedInt(reply[1]) + "." + Byte.toUnsignedInt(reply[0]);
                logger.debug("IP: '{}'", m_ip);

                m_port = String.valueOf(bytes2port(Arrays.copyOfRange(reply, 4, 8)));
                logger.debug("Port: '{}'", m_port);

                m_sn = new String(reply, 8, 40 - 8, StandardCharsets.UTF_8);
                logger.debug("SN: '{}'", m_sn);

                logger.trace("SSID length: '{}'", Byte.toUnsignedInt(reply[40]));

                m_ssid = new String(reply, 41, reply[40], StandardCharsets.UTF_8);
                logger.debug("SSID: '{}'", m_ssid);

                m_type = m_ssid.split("_")[1];
                logger.debug("Type: '{}'", m_type);

                // TODO:
                // m_support = support_test(m_ip, int(m_id), int(m_port))

                String thingName = createThingName(packet.getAddress().getAddress(), m_id, m_ssid);
                ThingUID thingUID = new ThingUID(THING_TYPE_MIDEAAC, thingName.toLowerCase());
                thingDiscovered(DiscoveryResultBuilder.create(thingUID).withLabel(thingName)
                        .withRepresentationProperty(CONFIG_IP).withThingType(THING_TYPE_MIDEAAC)
                        .withProperties(collectProperties(ipAddress, m_version, m_id, m_port, m_sn, m_ssid, m_type))
                        .build());
            }

            // TODO:
            // if data[:6].hex() == '3c3f786d6c20':
            // m_version = 'V1'
            // root=ET.fromstring(data.decode(encoding="utf-8", errors="replace"))
            // child = root.find('body/device')
            // m=child.attrib
            // m_port, m_sn, m_type = m['port'], m['apc_sn'], str(hex(int(m['apc_type'])))[2:]
            // response = get_device_info(m_ip, int(m_port))
            // m_id = get_id_from_response(response)
            //
            // _LOGGER.info(
            // "*** Found a {} device - type: '0x{}' - version: {} - ip: {} - port: {} - id: {} - sn: {} - ssid:
            // {}".format(m_support, m_type, m_version, m_ip, m_port, m_id, m_sn, m_ssid))

        }
    }

    private int bytes2port(byte[] bytes) {
        int b = 0;
        int i = 0;
        while (b < 4) {
            int b1;
            if (b < bytes.length) {
                b1 = bytes[b] & 0xFF;
            } else {
                b1 = 0;
            }

            i |= b1 << b * 8;
            b += 1;
        }
        return i;
    }

    /**
     * Creates a name for the Midea AC device.
     *
     * @param byteMac mac address in bytes
     * @return the name for the device
     */
    private String createThingName(final byte[] byteIP, String id, String ssid) {
        return MIDEAAC_NAME_PREFIX + "__" + Byte.toUnsignedInt(byteIP[0]) + "_" + Byte.toUnsignedInt(byteIP[1]) + "_"
                + Byte.toUnsignedInt(byteIP[2]) + "_" + Byte.toUnsignedInt(byteIP[3]) + "__" + id + "__" + ssid;
    }

    /**
     * Collects properties into a map.
     *
     * @param ipAddress IP address of the thing
     * @param mac mac address of the thing
     * @return map with properties
     */
    private Map<String, Object> collectProperties(String ipAddress, String version, String id, String port, String sn,
            String ssid, String type) {
        final Map<String, Object> properties = new TreeMap<>();
        properties.put(CONFIG_IP, ipAddress);
        properties.put(CONFIG_PORT, port);
        properties.put(CONFIG_DEVICEID, id);
        properties.put(CONFIG_POLLING_TIME, 10);
        properties.put(CONFIG_PROMPT_TONE, false);
        properties.put(PROPERTY_VERSION, version);
        properties.put(PROPERTY_SN, sn);
        properties.put(PROPERTY_SSID, ssid);
        properties.put(PROPERTY_TYPE, type);

        return properties;
    }
}
