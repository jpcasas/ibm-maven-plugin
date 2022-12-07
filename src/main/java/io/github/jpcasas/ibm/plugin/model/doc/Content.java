package io.github.jpcasas.ibm.plugin.model.doc;

import java.util.ArrayList;
import java.util.List;

public class Content {

    private String type;
    private List<ProjectFile> files;

    public Content(String type) {
        this.type = type;
        this.files = new ArrayList<>();
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ProjectFile> getFiles() {
        return this.files;
    }

    public void setFiles(List<ProjectFile> files) {
        this.files = files;
    }

    public void addProjectFile(ProjectFile projectFile) {
        files.add(projectFile);
    }

    public void addAllProjectFile(List<ProjectFile> files2) {
        files.addAll(files2);
    }

}
