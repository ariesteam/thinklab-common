/**
 * Copyright 2011 The ARIES Consortium (http://www.ariesonline.org) and
 * www.integratedmodelling.org. 

   This file is part of Thinklab.

   Thinklab is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published
   by the Free Software Foundation, either version 3 of the License,
   or (at your option) any later version.

   Thinklab is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Thinklab.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.integratedmodelling.thinklab.common.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.integratedmodelling.exceptions.ThinklabIOException;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;

/**
 * Simple log appender for debugging. Simplest way to use is Debug.print(....) which
 * will append the string to $HOME/debug.txt.
 * 
 * @author Ferd
 *
 */
public class Debug {

	private File fname;
	static Debug _debug;
	
	public Debug(String file, boolean append) throws ThinklabIOException {
		
		this.fname = new File(file);
		
		if (!append && this.fname.exists()) {
			this.fname.delete();
		}
	}
	
	public void print(String s)  {

		try {
			FileWriter writer = new FileWriter(fname, true);
			writer.write(s + System.getProperty("line.separator"));
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new ThinklabRuntimeException(e);
		}
	}
	
	public static void println(String s) {
		if (_debug == null) {
			try {
				_debug = new Debug(System.getProperty("user.home") + File.separator + "debug.txt", true);
			} catch (ThinklabIOException e) {
			};
		}
		_debug.print(s);
	}
}
