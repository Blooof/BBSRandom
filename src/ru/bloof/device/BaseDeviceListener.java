package ru.bloof.device;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:blloof@gmail.com">Oleg Larionov</a>
 */
public abstract class BaseDeviceListener implements DeviceEventListener {
    protected final ExecutorService executor;

    public BaseDeviceListener() {
        List<File> devices;
        try {
            devices = Files.walk(Paths.get("/dev/input"))
                    .map(Path::toFile)
                    .filter(file -> file.getName().startsWith("event"))
                    .filter(File::canRead)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (devices.isEmpty()) {
            throw new RuntimeException("No input devices");
        }
        executor = Executors.newFixedThreadPool(devices.size());
        for (File f : devices) {
            executor.execute(new DeviceReader(f, this));
        }
    }

    public void close() {
        executor.shutdownNow();
    }
}
