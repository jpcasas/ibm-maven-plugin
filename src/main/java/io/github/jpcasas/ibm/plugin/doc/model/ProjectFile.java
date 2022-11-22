package io.github.jpcasas.ibm.plugin.doc.model;

public class ProjectFile {
    private String schema;
    private String name;

    public ProjectFile(String schema, String name) {
        this.schema = schema;
        this.name = name;
    }

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
