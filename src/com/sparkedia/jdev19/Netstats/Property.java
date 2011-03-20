package com.sparkedia.jdev19.Netstats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Property {
	private Logger log;
	protected Netstats plugin;
	private LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
	private String fileName;
	private String pName;

	public Property(String fileName, Netstats plugin) {
		this.plugin = plugin;
		this.pName = plugin.pName;
		this.log = plugin.log;
		this.fileName = fileName;
		File file = new File(fileName);

		if (file.exists()) {
			load();
		} else {
			save();
		}
	}

	public void load() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) != null) {
				if ((line.trim().length() == 0) || (line.charAt(0) == '#')) {
					continue;
				}
				int delim = line.indexOf('=');
				String key = line.substring(0, delim).trim();
				String value = line.substring(delim+1).trim();
				this.properties.put(key, value);
			}
		} catch (FileNotFoundException ex) {
			log.log(Level.SEVERE, "["+pName+"]: Couldn't find file "+fileName, ex);
			return;
		} catch (IOException ex) {
			log.log(Level.SEVERE, "["+pName+"]: Unable to save "+fileName, ex);
			return;
		} finally {
			// Close the reader
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ex) {
				log.log(Level.SEVERE, "["+pName+"]: Unable to save "+fileName, ex);
			}
		}
	}

	public void save() {
		BufferedWriter bufferedWriter = null;
		try {
			// Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new FileWriter(fileName));
			bufferedWriter.write("# Netstats Properties File");
			bufferedWriter.newLine();
			
			// Save all the properties one at a time if there's data to write
			if (this.properties.size() > 0) {
				Set<?> set = this.properties.entrySet();
				Iterator<?> i = set.iterator();
				while (i.hasNext()) {
					Map.Entry<?, ?> me = (Map.Entry<?, ?>)i.next();
					String key = (String)me.getKey();
					String val = (String)me.getValue();
					bufferedWriter.write(key+"="+val);
					bufferedWriter.newLine();
				}
			}
		} catch (FileNotFoundException ex) {
			log.log(Level.SEVERE, "["+pName+"]: Couldn't find file "+fileName, ex);
			return;
		} catch (IOException ex) {
			log.log(Level.SEVERE, "["+pName+"]: Unable to save "+fileName, ex);
			return;
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				log.log(Level.SEVERE, "["+pName+"]: Unable to save "+fileName, ex);
			}
		}
	}
	
	public boolean keyExists(String key) {
		return this.properties.containsKey(key);
	}
	
	public void inc(String key) {
		this.properties.put(key, String.valueOf(Integer.parseInt((String)this.properties.get(key))+1));
	}
	
	// STRING
	public String getString(String key) {
		if (this.properties.containsKey(key)) {
			return (String)this.properties.get(key);
		}
		return "";
	}
	public void setString(String key, String value) {
		this.properties.put(key, value);
		save();
	}
	
	// INT
	public int getInt(String key) {
		if (this.properties.containsKey(key)) {
			return Integer.parseInt((String)this.properties.get(key));
		}
		return 0;
	}
	public void setInt(String key, int value) {
		this.properties.put(key, String.valueOf(value));
		save();
	}
	
	// DOUBLE
	public double getDouble(String key) {
		if (this.properties.containsKey(key)) {
			return Double.parseDouble((String)this.properties.get(key));
		}
		return 0.0D;
	}
	public void setDouble(String key, double value) {
		this.properties.put(key, String.valueOf(value));
		save();
	}
	
	// LONG
	public long getLong(String key) {
		if (this.properties.containsKey(key)) {
			return Long.parseLong((String)this.properties.get(key));
		}
		return 0L;
	}
	public void setLong(String key, long value) {
		this.properties.put(key, String.valueOf(value));
		save();
	}
	
	// FLOAT
	public float getFloat(String key) {
		if (this.properties.containsKey(key)) {
			return Float.parseFloat((String)this.properties.get(key));
		}
		return 0F;
	}
	public void setFloat(String key, float value) {
		this.properties.put(key, String.valueOf(value));
		save();
	}
	
	// BOOLEAN
	public boolean getBoolean(String key) {
		if (this.properties.containsKey(key)) {
			return Boolean.parseBoolean((String)this.properties.get(key));
		}
		return false;
	}
	public void setBoolean(String key, boolean value) {
		this.properties.put(key, String.valueOf(value));
		save();
	}
	
}