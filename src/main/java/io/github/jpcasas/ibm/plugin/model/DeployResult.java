package io.github.jpcasas.ibm.plugin.model;

import lombok.Data;

@Data
public class DeployResult {

    java.lang.String count;

    LogEntry[] LogEntry;

    java.lang.String type;

    java.lang.String uri;
}
