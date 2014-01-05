package jGalapagos.master;

import jGalapagos.core.Module;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * 
 * @author Mihej Komar
 *
 */
public class ModuleContainer {

	private final File directory;
	private final PropertiesConfiguration config;
	private final URLClassLoader urlClassLoader;
	private final Module module;
	private final Configuration defaultTopologyConfig;
	private final File defaultAlgorithmConfiguration;

	public ModuleContainer(File directory) throws Exception {
		this.directory = directory;
		this.config = new PropertiesConfiguration(new File(directory, "module.conf"));

		String[] jars = config.getStringArray("jars");
		URL[] urls = new URL[jars.length];
		for (int i = 0; i < jars.length; i++) {
			urls[i] = new URL("file:" + new File(directory, jars[i]).getPath());
		}

		urlClassLoader = new URLClassLoader(urls);
		Class<?> classApp = Class.forName(config.getString("moduleClass"), true, urlClassLoader);
		Object object = classApp.newInstance();
		module = (Module) object;
		defaultTopologyConfig = config.subset("defaultTopology");
		defaultAlgorithmConfiguration = new File(config.getString("defaultAlgorithmConfiguration"));
	}

	public File getDirectory() {
		return directory;
	}

	public PropertiesConfiguration getConfig() {
		return config;
	}

	public URLClassLoader getUrlClassLoader() {
		return urlClassLoader;
	}

	public Module getModule() {
		return module;
	}

	public Configuration getDefaultTopologyConfig() {
		return defaultTopologyConfig;
	}

	public File getDefaultAlgorithmConfiguration() {
		return defaultAlgorithmConfiguration;
	}

	@Override
	public String toString() {
		return config.getString("name");
	}

}