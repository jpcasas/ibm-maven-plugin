package io.github.jpcasas.ibm.plugin.model.ecliipse;



import java.util.List;

import lombok.Data;

@Data
public class ProjectDescriptor {
    String name;
	Object comment;
	List<String> projects;
	List<String> natures;
    
}
