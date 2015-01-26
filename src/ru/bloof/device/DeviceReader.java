package ru.bloof.device;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author <a href="mailto:blloof@gmail.com">Oleg Larionov</a>
 */
public class DeviceReader implements Runnable {
    private static final int EVENT_SIZE = 24;
    private static final int POLL_PERIOD = 100;
    private final File file;
    private final DeviceEventListener listener;

    public DeviceReader(File file, DeviceEventListener listener) {
        this.file = file;
        this.listener = listener;
    }

    @Override
    public void run() {
        byte[] eventData = new byte[EVENT_SIZE];
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            while (!Thread.interrupted()) {
                if (input.read(eventData) != eventData.length) {
                    Thread.sleep(POLL_PERIOD);
                    continue;
                }
                ByteBuffer bb = ByteBuffer.wrap(eventData);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                listener.onEvent(new DeviceEvent(bb));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
