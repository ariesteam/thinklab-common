package org.integratedmodelling.thinklab.common.configuration;

public class Env {

	/*
	 * default property names
	 */
	public static final String THINKLAB_HOME_PROPERTY = "thinklab.home";
	
	/**
	 * The only variable that does not have a default.
	 */
	public static final String ENV_THINKLAB_INSTALL_DIR = "THINKLAB_INSTALL_DIR";
	
	/**
	 * defaults to ${HOME}/.thinklab
	 */
	public static final String ENV_THINKLAB_DATA_DIR = "THINKLAB_DATA_DIR";

	/**
	 * defaults to ${HOME}/thinklab
	 */
	public static final String ENV_THINKLAB_WORKSPACE = "THINKLAB_WORKSPACE_DIR";

	/**
	 * defaults to THINKLAB_DATA_DIR/.scratch
	 */
	public static final String ENV_THINKLAB_SCRATCH_DIR = "THINKLAB_SCRATCH_DIR";
	
	/**
	 * defaults to THINKLAB_INSTALL_DIR/plugins
	 */
	public static final String ENV_THINKLAB_PLUGIN_DIR = "THINKLAB_PLUGIN_DIR";

}
