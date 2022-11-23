package io.github.jpcasas.ibm.plugin.common;

import java.util.Arrays;
import java.util.List;

public interface CommonConstants {

       



        

        String PROJECT_TYPE_ACE_APPLICATION = "app";
        String PROJECT_TYPE_ACE_SHARED_LIBRARY = "shlib";
        String PROJECT_TYPE_ACE_STATIC_LIBRARY = "stlib";
        String PROJECT_TYPE_ACE_JAVA = "java";
        String PROJECT_TYPE_ACE_POLICY = "policy";
        String PROJECT_TYPE_APIC = "apic";

        List<String> PROJECT_TYPES = Arrays.asList(PROJECT_TYPE_ACE_APPLICATION, PROJECT_TYPE_ACE_SHARED_LIBRARY,
                        PROJECT_TYPE_ACE_STATIC_LIBRARY, PROJECT_TYPE_ACE_JAVA, PROJECT_TYPE_ACE_POLICY,
                        PROJECT_TYPE_APIC);

        

}

