package com.tasky.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.tasky.model.AppData;
import com.tasky.model.TaskList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataService {
    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = "tasky_data.json";
    private final Gson gson;
    private final Path dataFilePath;

    public DataService() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        Path dataDir = Paths.get(DATA_DIR);
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.dataFilePath = dataDir.resolve(DATA_FILE);
    }

    public void save(AppData data) {
        try (Writer writer = new FileWriter(dataFilePath.toFile())) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save data: " + e.getMessage());
        }
    }

    public AppData load() {
        if (!Files.exists(dataFilePath)) {
            return createDefaultData();
        }

        try (Reader reader = new FileReader(dataFilePath.toFile())) {
            AppData data = gson.fromJson(reader, AppData.class);
            if (data == null) {
                return createDefaultData();
            }
            ensureInboxExists(data);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return createDefaultData();
        }
    }

    private AppData createDefaultData() {
        AppData data = new AppData();
        data.getLists().add(TaskList.createInbox());
        return data;
    }

    private void ensureInboxExists(AppData data) {
        boolean hasInbox = data.getLists().stream()
                .anyMatch(list -> TaskList.INBOX_ID.equals(list.getId()));
        if (!hasInbox) {
            data.getLists().add(0, TaskList.createInbox());
        }
    }

    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(value));
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDate.parse(in.nextString(), formatter);
        }
    }

    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(value));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString(), formatter);
        }
    }
}
