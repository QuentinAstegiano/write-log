package org.astegiano.wl.data;

import java.time.LocalDate;

/**
 * Created by Quentin Astegiano on 19/10/2015.
 */
public class LogEntry {

    public final String id;
    public final String title;
    public final String content;
    public final LocalDate date;

    public LogEntry(String id, String title, String content, LocalDate date) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
    }
}
