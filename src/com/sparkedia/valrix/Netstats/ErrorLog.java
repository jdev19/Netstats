package com.sparkedia.valrix.Netstats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ErrorLog {
	private Logger log;
	protected Netstats plugin;
	private LinkedHashMap<String, Object> output = new LinkedHashMap<String, Object>();
	private String file;
	private String pName;

	public ErrorLog(String file, Netstats plugin) {
		this.plugin = plugin;
		this.pName = plugin.pName;
		this.log = plugin.log;
		this.file = file;
		
		if (!(new File(file).exists())) {
			save();
		}
	}
	
	// Save data from LinkedHashMap to file
	public void save() {
		BufferedWriter bw = null;
		try {
			// Construct the BufferedWriter object
			bw = new BufferedWriter(new FileWriter(file));
			bw.write("# "+pName+" Error Log");
			bw.newLine();
			
			// Save all the properties one at a time, only if there's data to write
			if (output.size() > 0) {
				// Grab all the entries and create an iterator to run through them all
				Set<?> set = output.entrySet();
				Iterator<?> i = set.iterator();
				
				// While there's data to iterate through..
				while (i.hasNext()) {
					// Map the entry and save the key and value as variables
					Map.Entry<?, ?> me = (Map.Entry<?, ?>)i.next();
					String key = (String)me.getKey();
					String val = me.getValue().toString();
					
					// If it starts with "com", it's a comment so write it as such
					if (key.startsWith("com")) {
						// Writing a comment to the file
						bw.write("# "+val);
						bw.newLine();
					} else {
						// Otherwise write the key and value pair as key=value
						bw.write(key+": "+val);
						bw.newLine();
					}
				}
			}
		} catch (FileNotFoundException ex) {
			log.log(Level.SEVERE, '['+pName+"]: Couldn't find file "+file, ex);
			return;
		} catch (IOException ex) {
			log.log(Level.SEVERE, '['+pName+"]: Unable to save "+file, ex);
			return;
		} finally {
			// Close the BufferedWriter
			try {
				if (bw != null) {
					bw.flush();
					bw.close();
				}
			} catch (IOException ex) {
				log.log(Level.SEVERE, '['+pName+"]: Unable to save "+file, ex);
			}
		}
	}
	
	// Set output value as a string
	public void setString(String key, String value) {
		output.put(key, value);
	}
	
	// Set output value as an integer
	public void setInt(String key, int value) {
		output.put(key, String.valueOf(value));
		save();
	}
	
	// Set output value as a double
	public void setDouble(String key, double value) {
		output.put(key, String.valueOf(value));
	}
	
	// Set output value as a long
	public void setLong(String key, long value) {
		output.put(key, String.valueOf(value));
	}
	
	// Set output value as a float
	public void setFloat(String key, float value) {
		output.put(key, String.valueOf(value));
	}
	
	// Set output value as a boolean
	public void setBoolean(String key, boolean value) {
		output.put(key, String.valueOf(value));
	}
}