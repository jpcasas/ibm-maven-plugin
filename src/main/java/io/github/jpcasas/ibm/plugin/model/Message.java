package io.github.jpcasas.ibm.plugin.model;

import lombok.Data;

@Data
public class Message {
    java.lang.String timestamp;
  
    java.lang.String threadId;
    
    java.lang.String inserts;
    
    java.lang.String source;
    
    java.lang.String severity;
    
    java.lang.String number;
    
    java.lang.String severityCode;
    
    java.lang.String threadSequenceNumber;
}
