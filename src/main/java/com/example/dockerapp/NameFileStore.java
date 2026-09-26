package com.example.dockerapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NameFileStore {

    private final Path dataFile;
    private final ObjectMapper objectMapper;
    private final ReentrantLock processLock = new ReentrantLock();

    public NameFileStore(@Value("${app.data-file}") String dataFile, ObjectMapper objectMapper) {
        this.dataFile = Path.of(dataFile).toAbsolutePath();
        this.objectMapper = objectMapper;
    }

    public void save(String name) throws IOException {
        withLockedFile(channel -> {
            channel.position(channel.size());
            byte[] record = (objectMapper.writeValueAsString(name) + "\n")
                    .getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.wrap(record);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            return null;
        });
    }

    public List<String> getAll() throws IOException {
        return withLockedFile(channel -> {
            channel.position(0);
            ByteArrayOutputStream contents = new ByteArrayOutputStream();
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            while (channel.read(buffer) != -1) {
                buffer.flip();
                contents.write(buffer.array(), buffer.position(), buffer.remaining());
                buffer.clear();
            }

            List<String> names = new ArrayList<>();
            for (String record : contents.toString(StandardCharsets.UTF_8).split("\\R")) {
                if (!record.isBlank()) {
                    names.add(objectMapper.readValue(record, String.class));
                }
            }
            return List.copyOf(names);
        });
    }

    private <T> T withLockedFile(FileOperation<T> operation) throws IOException {
        processLock.lock();
        try {
            Files.createDirectories(dataFile.getParent());
            try (FileChannel channel = FileChannel.open(dataFile,
                    StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
                    FileLock ignored = channel.lock()) {
                return operation.apply(channel);
            }
        } finally {
            processLock.unlock();
        }
    }

    @FunctionalInterface
    private interface FileOperation<T> {
        T apply(FileChannel channel) throws IOException;
    }
}