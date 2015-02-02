package ru.bloof.device;

import java.nio.ShortBuffer;

/**
 * @author <a href="mailto:blloof@gmail.com">Oleg Larionov</a>
 */
public class DeviceEvent {
    public static final short EV_SYN = 0x00;
    public static final short EV_KEY = 0x01;

    private final long timeSec, timeUsec;
    private final short type, code;
    private final int value;

    public DeviceEvent(long timeSec, long timeUsec, short type, short code, int value) {
        this.timeSec = timeSec;
        this.timeUsec = timeUsec;
        this.type = type;
        this.code = code;
        this.value = value;
    }

    public DeviceEvent(ShortBuffer sb) {
        short a, b, c, d;
        a = sb.get();
        b = sb.get();
        c = sb.get();
        d = sb.get();
        timeSec = ((long) d << 48) | ((long) c << 32) | ((long) b << 16) | a;
        a = sb.get();
        b = sb.get();
        c = sb.get();
        d = sb.get();
        timeUsec = ((long) d << 48) | ((long) c << 32) | ((long) b << 16) | a;
        type = sb.get();
        code = sb.get();
        a = sb.get();
        b = sb.get();
        value = ((int) b << 16) | a;
    }

    public long getTimeSec() {
        return timeSec;
    }

    public long getTimeUsec() {
        return timeUsec;
    }

    public short getType() {
        return type;
    }

    public short getCode() {
        return code;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Event{" +
                "timeSec=" + timeSec +
                ", timeUsec=" + timeUsec +
                ", type=" + type +
                ", code=" + code +
                ", value=" + value +
                '}';
    }
}
