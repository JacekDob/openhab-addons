package org.openhab.binding.mideaac.internal.handler;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Timer.
 *
 * @author Jacek Dobrowolski
 */
public class Timer {
    private boolean status;
    private int hours;
    private int minutes;

    public Timer(boolean status, int hours, int minutes) {
        this.status = status;
        this.hours = hours;
        this.minutes = minutes;
    }

    public boolean getStatus() {
        return status;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    @Override
    public @NonNull String toString() {
        if (status) {
            return String.format("enabled: %s, hours: %d, minutes: %d", status, hours, minutes);
        } else {
            return String.format("enabled: %s", status);
        }
    }

    public @NonNull String toChannel() {
        if (status) {
            return String.format("%02d:%02d", hours, minutes);
        } else {
            return "";
        }
    }

}
