package com.sparkedia.jdev19.Netstats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Property {
	private Logger log;
	protected Netstats plugin;
	private Properties properties;
	private String fileName;
	private String pName;

	public Property(String fileName, Netstats plugin) {
		this.plugin = plugin;
		this.pName = plugin.pName;
		this.log = plugin.log;
		this.fileName = fileName;
		this.properties = new Properties();
		File file = new File(fileName);

		if (file.exists()) {
			load();
		} else {
			save();
		}
	}

	public void load() {
		try {
			FileInputStream inFile = new FileInputStream(fileName);
			this.properties.load(inFile);
			inFile.close(); // ALWAYS close a file when done with it
		} catch (IOException ex) {
			log.log(Level.SEVERE, "["+pName+"]: Unable to load "+fileName, ex);
		}
	}

	public void save() {
		try {
			FileOutputStream outFile = new FileOutputStream(fileName);
			this.properties.store(outFile, "Minecraft Properties File");
			outFile.close();
		} catch (IOException ex) {
			log.log(Level.SEVERE, "["+pName+"]: Unable to save "+fileName, ex);
		}
	}

	public Map<String, String> returnMap() throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line;
		while ((line = reader.readLine()) != null) {
			if ((line.trim().length() == 0) || 
					(line.charAt(0) == '#')) {
				continue;
			}
			int delimPosition = line.indexOf('=');
			String key = line.substring(0, delimPosition).trim();
			String value = line.substring(delimPosition + 1).trim();
			map.put(key, value);
		}
		reader.close();
		return map;
	}

	public boolean keyExists(String key) {
		return this.properties.containsKey(key);
	}
	
	public void inc(String key) {
		this.properties.setProperty(key, this.properties.getProperty(key)+1);
	}
	
	// STRING
	public String getString(String key) {
		if (this.properties.containsKey(key)) {
			return this.properties.getProperty(key);
		}
		return "";
	}
	public void setString(String key, String value) {
		this.properties.setProperty(key, value);
		save();
	}
	
	// INT
	public int getInt(String key) {
		if (this.properties.containsKey(key)) {
			return Integer.parseInt(this.properties.getProperty(key));
		}
		return 0;
	}
	public void setInt(String key, int value) {
		this.properties.setProperty(key, String.valueOf(value));
		save();
	}
	
	// DOUBLE
	public double getDouble(String key) {
		if (this.properties.containsKey(key)) {
			return Double.parseDouble(this.properties.getProperty(key));
		}
		return 0.0D;
	}
	public void setDouble(String key, double value) {
		this.properties.setProperty(key, String.valueOf(value));
		save();
	}
	
	// LONG
	public long getLong(String key) {
		if (this.properties.containsKey(key)) {
			return Long.parseLong(this.properties.getProperty(key));
		}
		return 0L;
	}
	public void setLong(String key, long value) {
		this.properties.setProperty(key, String.valueOf(value));
		save();
	}
	
	// FLOAT
	public float getFloat(String key) {
		if (this.properties.containsKey(key)) {
			return Float.parseFloat(this.properties.getProperty(key));
		}
		return 0F;
	}
	public void setFloat(String key, float value) {
		this.properties.setProperty(key, String.valueOf(value));
		save();
	}
	
	// BOOLEAN
	public boolean getBoolean(String key) {
		if (this.properties.containsKey(key)) {
			return Boolean.parseBoolean(this.properties.getProperty(key));
		}
		return false;
	}
	public void setBoolean(String key, boolean value) {
		this.properties.setProperty(key, String.valueOf(value));
		save();
	}
}