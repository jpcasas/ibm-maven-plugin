package io.github.jpcasas.ibm.plugin.common;

import java.util.Arrays;
import java.util.List;

public interface CommonConstants {

       

        List<String> ORG_WITH_LIFE_CYCLES = Arrays.asList(ORG_ACP, ORG_PRD);

        String TASK_APPROVAL_PUBLISH = "published";
        String TASK_APPROVAL_REPLACE = "replace";
        String TASK_APPROVAL_SUPERSEDE = "supersede";

        String TASK_APPROVAL_STATUS_APPROVED = "approved";

        String PRODUCT_OPERATION_PUBLISH = "publish";
        String PRODUCT_OPERATION_REPLACE = "replace";
        String PRODUCT_OPERATION_SUPERSEDE = "supersede";
        String PRODUCT_OPERATION_SUBSCRIBE = "subscribe";

        List<String> PRODUCT_PUBLISHING_OPERATIONS = Arrays.asList(PRODUCT_OPERATION_PUBLISH, PRODUCT_OPERATION_REPLACE,
                        PRODUCT_OPERATION_SUPERSEDE);

        String PROVIDER_OPERATION_CREATE = "create";
        String PROVIDER_OPERATION_UPDATE = "update";

        String PROJECT_TYPE_ACE_APPLICATION = "app";
        String PROJECT_TYPE_ACE_SHARED_LIBRARY = "shlib";
        String PROJECT_TYPE_ACE_STATIC_LIBRARY = "stlib";
        String PROJECT_TYPE_ACE_JAVA = "java";
        String PROJECT_TYPE_ACE_POLICY = "policy";
        String PROJECT_TYPE_APIC = "apic";

        List<String> PROJECT_TYPES = Arrays.asList(PROJECT_TYPE_ACE_APPLICATION, PROJECT_TYPE_ACE_SHARED_LIBRARY,
                        PROJECT_TYPE_ACE_STATIC_LIBRARY, PROJECT_TYPE_ACE_JAVA, PROJECT_TYPE_ACE_POLICY,
                        PROJECT_TYPE_APIC);

        String DIR_APIC_RESOURCES = "resources";
        String DIR_APIC_SOURCES = "src";

        List<String> GENERATE_APIC_DIRECTORIES = Arrays.asList(DIR_APIC_RESOURCES, DIR_APIC_SOURCES);

        String PRODUCT_STATE_PUBLISHED = "published";
        String PRODUCT_STATE_DEPRECATED = "deprecated";
        String PRODUCT_STATE_RETIRED = "retired";
        String PRODUCT_STATE_STAGED = "staged";

        String DEFAULT_PLAN_NAME = "default-plan";

        String MAJOR_VERSION = "major";
        String MINOR_VERSION = "minor";
        String PATCH_VERSION = "patch";

        String REGEX_PATTERN_SEMVER = "^[1-9]\\d*\\.\\d+\\.\\d+";

}

