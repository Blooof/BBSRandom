package ru.bloof.device;

import java.nio.ByteBuffer;

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

    public DeviceEvent(ByteBuffer bb) {
        this(bb.getLong(), bb.getLong(), bb.getShort(), bb.getShort(), bb.getInt());
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
}
