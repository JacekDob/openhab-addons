# MideaAC Binding

This is the binding for [Midea Air Conditioner](https://www.midea.com/us/Air-Conditioners).
This binding allows you to integrate, view and control Midea Air Conditioner in the openHAB environment in LAN without Midea Cloud.

Thank you for sharing as it it based on:
[mac-zhou/midea-msmart](https://github.com/mac-zhou/midea-msmart/tree/master/msmart)

## Supported Things

Device supported

| Thing type               | Name                                               |
|--------------------------|----------------------------------------------------|
| midea:ac                 | Midea Air Conditioner v. 2 and v. 3                |

## Discovery

All devices in LAN are discovered automatically.

### Things

Minimal Thing configuration:

```
UID: mideaac:ac:mideaac__192_168_x_x__12345678901234__net_ac_abcd
label: LABEL
thingTypeUID: mideaac:ac
configuration:
  ipAddress: 192.168.x.x
  ipPort: "6444"
  deviceId: "12345678901234"
```

### Channels

Following items are tested and working:

| channel                  | type                 | description                                                                                                      | read only |
|--------------------------|----------------------|------------------------------------------------------------------------------------------------------------------|-----------|
| Power                    | Switch               | Turn the AC on and off.                                                                                          |           |
| Target temperature       | Number:Temperature   | Target temperature.                                                                                              |           |
| Operational mode         | String               | Operational mode: OFF (turns off), AUTO, COOL, DRY, HEAT.                                                        |           |
| Fan speed                | String               | Fan speed: OFF (turns off), SILENT, LOW, MEDIUM, HIGH, AUTO.                                                     |           |
| Swing mode               | String               | Swing mode: OFF, VERTICAL, HORIZONTAL, BOTH.                                                                     |           |
| Eco mode                 | Switch               | Eco mode, according to manual works only in COOL mode (temperature shall be set to 24C and fan on AUTO).         | yes       |
| Turbo mode               | Switch               | Turbo mode, "Boost" in Midea Air app, long press "+" on IR Remote Controller. Only works in COOL and HEAT mode.  |           |
| Indoor temperature       | Number:Temperature   | Indoor temperature measured in the room, where internal unit is installed.                                       | yes       |
| Outdoor temperature      | Number:Temperature   | Outdoor temperature measured outside, where external unit is installed.                                          | yes       |
| Sleep function           | Switch               | Sleep function ("Moon with a star" icon on IR Remote Controller).                                                | yes       |

Following items are in API but are not tested as working (marked as advanced channels):
- Screen display
- Imode resume
- Timer mode
- Appliance error
- ON Timer
- OFF Timer
- Cozy sleep
- Save
- Low frequency fan
- Super fan
- Feel own 
- Child sleep mode
- Exchange air
- Dry clean
- Aux heat
- Clean up
- Temperature unit
- Catch cold
- Night light
- Peak elec
- Natural fan
- Humidity

### Debugging and Tracing

If you want to see what's going on in the binding, switch the loglevel to TRACE in the Karaf console

```
log:set TRACE org.openhab.binding.mideaac
```

Set the logging back to normal

```
log:set INFO org.openhab.binding.mideaac
```
