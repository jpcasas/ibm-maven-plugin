package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.ibm.broker.MessageBrokerAPIException;
import com.ibm.broker.config.appdev.FlowRendererBAR;

/**
 *
 * @author Juan Pablo Casas
 */

@Mojo(name = "ace-bar", defaultPhase = LifecyclePhase.PACKAGE)
public class BarPackage extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

	@Parameter(defaultValue = "true", property = "deployAsSource", required = true)
	private boolean deployAsSource;

	@Parameter(defaultValue = "false", property = "trace", required = true)
	private boolean trace;

	@Parameter(defaultValue = ".bar", property = "ibm.ace.extension", required = true)
	private String extension;

	@Parameter(defaultValue = "false", property = "ibm.easter.egg", required = false)
	private boolean ascii;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("----------------------------------------------------");
		getLog().info("               IBM ACE Packaging Bar                ");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");
		if (ascii) {
			String sb = " '             (`-')  _           (`-')  _     _(`-')    (`-')  _      (`-')  _ (`-').->(`-')     (`-')  _                                    (`-')  _     <-.(`-')              _             _(`-')      '  + "
					+ " '      <-.    (OO ).-/          _(OO ) (_)   ( (OO ).-> (OO ).-/      ( OO).-/ ( OO)_  ( OO).->  (OO ).-/        <-.        .->    _         (OO ).-/      __( OO)      .->    (_)      <-.  ( (OO ).->   '  + "
					+ " '    ,--. )   / ,---.      ,--.(_/,-.\\ ,-(`-')\\    .'_  / ,---.      (,------.(_)--\\_) /    '._  / ,---.       ,--. )  (`-')----.  \\-,-----. / ,---.      '-'---.\\ ,--.(,--.   ,-(`-'),--. )  \\    .'_    '  + "
					+ " '    |  (`-') | \\ /`.\\     \\   \\ / (_/ | ( OO)'`'-..__) | \\ /`.\\      |  .---'/    _ / |'--...__)| \\ /`.\\      |  (`-')( OO).-.  '  |  .--./ | \\ /`.\\     | .-. (/ |  | |(`-') | ( OO)|  (`-')'`'-..__)   '  + "
					+ " '    |  |OO ) '-'|_.' |     \\   /   /  |  |  )|  |  ' | '-'|_.' |    (|  '--. \\_..`--. `--.  .--''-'|_.' |     |  |OO )( _) | |  | /_) (`-') '-'|_.' |    | '-' `.)|  | |(OO ) |  |  )|  |OO )|  |  ' |   '  + "
					+ " '   (|  '__ |(|  .-.  |    _ \\     /_)(|  |_/ |  |  / :(|  .-.  |     |  .--' .-._)   \\   |  |  (|  .-.  |    (|  '__ | \\|  |)|  | ||  |OO )(|  .-.  |    | /`'.  ||  | | |  \\(|  |_/(|  '__ ||  |  / :   '  + "
					+ " '    |     |' |  | |  |    \\-'\\   /    |  |'->|  '-'  / |  | |  |     |  `---.\\       /   |  |   |  | |  |     |     |'  '  '-'  '(_'  '--'\\ |  | |  |    | '--'  /\\  '-'(_ .' |  |'->|     |'|  '-'  /   '  + "
					+ " '   `-----'  `--' `--'        `-'     `--'   `------'  `--' `--'     `------' `-----'    `--'   `--' `--'     `-----'    `-----'    `-----' `--' `--'    `------'  `-----'    `--'   `-----' `------'    ' ; ";

			getLog().info(sb);
		}
		try {
			File projectf = new File(project.getBasedir(), ".project");
			if (projectf.exists()) {
				String artifact = project.getArtifactId();
				getLog().info("        [BAR] Building " + artifact + "                   ");

				String workspace = outputDirectory.getCanonicalPath();
				ArrayList<String> individual = new ArrayList<String>();
				individual.add(artifact);
				getLog().info(workspace);
				getLog().info(artifact);
				FlowRendererBAR.write(workspace, individual, workspace, artifact + extension, 0, true);
				getLog().info(" ");
				getLog().info("(\"-------------------------END-------------------------");
			} else {
				throw new MojoFailureException("Project not found, Check if the .project exists into the folder");
			}
		} catch (IOException e) {
			throw new MojoFailureException("IOException", e);
		} catch (MessageBrokerAPIException e) {
			throw new MojoFailureException("MessageBrokerAPIException", e);

		}

	}
}
