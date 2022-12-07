package io.github.jpcasas.ibm.plugin.model;

import lombok.Data;

@Data
public class LogEntry {
    Message message;

    java.lang.String text;

    java.lang.String detailedText;

    java.lang.String type;

}
