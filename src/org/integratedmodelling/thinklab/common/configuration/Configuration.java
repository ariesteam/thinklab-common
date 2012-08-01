package org.integratedmodelling.thinklab.common.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.integratedmodelling.exceptions.ThinklabConfigurationException;
import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabIOException;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.api.configuration.IConfiguration;

/**
 * Global Thinklab configuration. Thinklab proxies to one instance
 * of this.
 * 
 * @author Ferd
 *
 */
public class Configuration implements IConfiguration {
	
	static OS os = null;
	
	private static Configuration _this;
	
	public static Configuration get() {
		if (_this == null) {
			try {
				_this = new Configuration();
			} catch (ThinklabException e) {
				throw new ThinklabRuntimeException(e);
			}
		}
		return _this;
	}
	
	/*
	 * all configurable paths; others are derived from them.
	 */
	File _installPath;
	File _scratchPath;
	File _dataPath;
	File _workspacePath;
	
	Properties _properties;
	
	public Configuration() throws ThinklabException {
		
		String installPath = System.getenv(Env.ENV_THINKLAB_INSTALL_DIR);
		
		/*
		 * try current directory before giving up.
		 */
		if (installPath == null) {
			if (new File("." + File.separator + "knowledge" + File.separator + "thinklab.owl").exists())
				installPath = ".";
		}
		
		if (installPath /* still */ == null) 
			throw new ThinklabConfigurationException(
				"Thinklab installation not found: the environmental variable " +
				Env.ENV_THINKLAB_INSTALL_DIR + 
				" must be defined");
	
		String home = System.getProperty("user.home");
		
		this._installPath = new File(installPath);
		this._dataPath = 
				getPath(Env.ENV_THINKLAB_DATA_DIR, 
						home + File.separator + ".thinklab");
		this._scratchPath = 
				getPath(Env.ENV_THINKLAB_SCRATCH_DIR, 
						this._dataPath + File.separator + ".scratch");
		this._workspacePath = 
				getPath(Env.ENV_THINKLAB_WORKSPACE, 
						home + File.separator + "thinklab");

	}
	
	/*
	 * create default unless var for first is there, make directories,
	 * complain if not there, return file.
	 */
	private File getPath(String env, String string) throws ThinklabException {

		String path = System.getenv(env);
		if (path == null)
			path = string;
		
		File f = new File(path);

		try {
			f.mkdirs();
		} catch (Exception e) {
		}
		
		if (!f.exists() || !f.isDirectory()) {
			throw new ThinklabIOException("cannot create or access system path " + path);
		}
		
		return f;
	}

	@Override
	public Properties getProperties() {
		
		if (_properties == null) {
			/*
			 * load or create thinklab system properties
			 */
			_properties = new Properties();
			File properties = 
					new File(getWorkspace(SUBSPACE_CONFIG) + File.separator + "thinklab.properties");
			try {
				if (properties.exists()) {
					FileInputStream input;

					input = new FileInputStream(properties);
					_properties.load(input);
					input.close();
				} else {
					FileUtils.touch(properties);
				}
			} catch (Exception e) {
				throw new ThinklabRuntimeException(e);
			}
		}
		return _properties;
	}

	public void persistProperties() throws ThinklabException {
		File td = 
				new File(getWorkspace(SUBSPACE_CONFIG) + File.separator + "thinklab.properties");
			
			try {
				getProperties().store(new FileOutputStream(td), null);
			} catch (Exception e) {
				throw new ThinklabIOException(e);
			}
			
	}
	
	@Override
	public File getWorkspace() {
		return _workspacePath;
	}

	@Override
	public File getWorkspace(String subspace) {
		File ret = new File(_workspacePath + File.separator + subspace);
		ret.mkdirs();
		return ret;
	}

	@Override
	public File getScratchArea() {
		return _scratchPath;
	}

	@Override
	public File getScratchArea(String subArea) {
		File ret = new File(_scratchPath + File.separator + subArea);
		ret.mkdirs();
		return ret;
	}

	@Override
	public File getTempArea(String subArea) {

		File ret = new File(_scratchPath + File.separator + "tmp");
		if (subArea != null) {
			ret = new File(ret + File.separator + subArea);
		}
		ret.mkdirs();
		return ret;
	}

	@Override
	public File getLoadPath() {
		return _installPath;
	}
	
	@Override
	public File getLoadPath(String subArea) {

		return new File(_installPath + File.separator + subArea);
	}
	
	/**
	 * Quickly and unreliably retrieve the class of OS we're running on.
	 * @return
	 */
	static public OS getOS() {

		if (os == null) {

			String osd = System.getProperty("os.name").toLowerCase();

			// TODO ALL these checks need careful checking
			if (osd.contains("windows")) {
				os = OS.WIN;
			} else if (osd.contains("mac")) {
				os = OS.MACOS;
			} else if (osd.contains("linux") || osd.contains("unix")) {
				os = OS.UNIX;
			}

		}

		return os;
	}

	public File getProjectDirectory() {
		return getWorkspace(Session.PROJECTS_WORKSPACE);
	}
	
	public File getProjectDirectory(String projectId) {
		return getWorkspace(Session.PROJECTS_WORKSPACE + File.separator + projectId);
	}

	public String getVersion() {
		// TODO tie to build mechanism
		return "1.0 rc1";
	}
	

}
