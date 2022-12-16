package io.github.jpcasas.ibm.plugin.model;

import lombok.Data;

@Data
public class MessageAssembly {

    String application;
    String flow;
    String node;

    public void set(String nodeName, String nodeValue) {
        String val = nodeValue.replace(" ", "_");
        if(nodeName.equals("application")){
            application = val;

        }
        if(nodeName.equals("messageFlow")){
            flow = val;
            
        }
        if(nodeName.equals("node")){
            node = val;
            
        }
    }

  
    
}
