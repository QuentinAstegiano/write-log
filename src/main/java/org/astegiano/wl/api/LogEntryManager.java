package org.astegiano.wl.api;

import org.astegiano.wl.data.LogEntry;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Quentin Astegiano on 19/10/2015.
 */
public class LogEntryManager {

    public final Map<String, LogEntry> map = new HashMap<>();

    public LogEntryManager() {
        map.put("test", new LogEntry("test", "Une entrée de test", "Un contenu de test", LocalDate.now()));
    }

    public Optional<LogEntry> get(String id) {
        return Optional.ofNullable(map.get(id));
    }
}
