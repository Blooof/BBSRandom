package ru.bloof.prng;

import ru.bloof.device.BaseDeviceListener;
import ru.bloof.device.DeviceEvent;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author <a href="mailto:blloof@gmail.com">Oleg Larionov</a>
 */
public class DeviceRandom extends Random {
    private final ArrayBlockingQueue<Byte> bytes = new ArrayBlockingQueue<>(1000);
    private final BaseDeviceListener listener = new BaseDeviceListener() {
        @Override
        public void onEvent(DeviceEvent event) {
            bytes.offer((byte) (event.getValue() & 0xFF));
            bytes.offer((byte) ((event.getValue() >> 8) & 0xFF));
        }
    };

    private byte getByte() throws InterruptedException {
        return bytes.take();
    }

    @Override
    public void nextBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            try {
                bytes[i] = getByte();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    protected int next(int bits) {
        int numBytes = (bits + 7) / 8;
        byte[] b = new byte[numBytes];
        int next = 0;
        nextBytes(b);
        for (int i = 0; i < numBytes; i++) {
            next = (next << 8) + (b[i] & 0xFF);
        }
        return next >>> (numBytes * 8 - bits);
    }

    public void close() {
        listener.close();
    }
}
