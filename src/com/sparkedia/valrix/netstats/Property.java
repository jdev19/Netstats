package com.sparkedia.valrix.netstats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
	private String filename;
	private String pName;

	public Property(String filename, Netstats plugin) {
		this.plugin = plugin;
		this.pName = plugin.pName;
		this.log = plugin.log;
		this.filename = filename;
		File file = new File(filename);

		if (file.exists()) {
			load();
		} else {
			save();
		}
	}
	
	// Load data from file into ordered HashMap
	public void load() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"UTF-8"));
			String line;
			int cc = 0; // # of comments
			int lc = 0; // # of lines
			
			// While there are lines to read
			while ((line = br.readLine()) != null) {
				// Line has content
				if (line.trim().length() == 0) {
					continue;
				}
				// If not the first line and is a comment, store it for later
				if (line.charAt(0) == '#' && lc > 0) {
					int delim = line.indexOf(' ');
					String key = "#"+cc;
					String val = line.substring(delim+1).trim();
					properties.put(key, val);
					cc++;
					continue;
				}
				// and isn't the first line of the file
				if (lc > 0) {
					int delim = 0;
					// Not the first line and isn't a comment, store the key and value
					while ((delim = line.indexOf('=')) != -1) {
						if (line.charAt(delim-1) != '\\') {
							String key = line.substring(0, delim).trim();
							String val = line.substring(delim+1).trim();
							properties.put(unescape(key), val);
							break;
						}
					}
				}
				lc++;
			}
		} catch (FileNotFoundException ex) {
			log.log(Level.SEVERE, '['+pName+"]: Couldn't find file "+filename, ex);
			return;
		} catch (IOException ex) {
			log.log(Level.SEVERE, '['+pName+"]: Unable to save "+filename, ex);
			return;
		} finally {
			// Close the reader
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				log.log(Level.SEVERE, '['+pName+"]: Unable to save "+filename, ex);
			}
		}
	}
	
	// Save data from LinkedHashMap to file
	public void save() {
		BufferedWriter bw = null;
		try {
			// Construct the BufferedWriter object
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8"));
			bw.write("# "+pName+" Properties File");
			bw.newLine();
			
			// Save all the properties one at a time, only if there's data to write
			if (properties.size() > 0) {
				// Grab all the entries and create an iterator to run through them all
				Set<?> set = properties.entrySet();
				Iterator<?> i = set.iterator();
				
				// While there's data to iterate through..
				while (i.hasNext()) {
					// Map the entry and save the key and value as variables
					Map.Entry<?, ?> me = (Map.Entry<?, ?>)i.next();
					String key = (String)me.getKey();
					String val = me.getValue().toString();
					
					// If it starts with "#", it's a comment so write it as such
					if (key.charAt(0) == '#') {
						// Writing a comment to the file
						bw.write("# "+val);
						bw.newLine();
					} else {
						// Otherwise write the key and value pair as key=value
						bw.write(escape(key)+'='+val);
						bw.newLine();
					}
				}
			}
		} catch (FileNotFoundException ex) {
			log.log(Level.SEVERE, '['+pName+"]: Couldn't find file "+filename, ex);
			return;
		} catch (IOException ex) {
			log.log(Level.SEVERE, '['+pName+"]: Unable to save "+filename, ex);
			return;
		} finally {
			// Close the BufferedWriter
			try {
				if (bw != null) {
					bw.flush();
					bw.close();
				}
			} catch (IOException ex) {
				log.log(Level.SEVERE, '['+pName+"]: Unable to save "+filename, ex);
			}
		}
	}
	
	// Rebuild the current properties file using data from newMap
	public void rebuild(LinkedHashMap<String, Object> newMap) {
		properties.clear();
		properties.putAll(newMap);
		save();
	}
	
	// Function to check if current properties file matches a referenced one by validating every key
	public boolean match(LinkedHashMap<String, Object> prop) {
		if (properties.keySet().containsAll(prop.keySet())) {
			return true;
		}
		return false;
	}
	
	// Check if the key exists or not
	public boolean keyExists(String key) {
		if(properties.containsKey(key)) {
			return true;
		}
		return false;
	}
	
	// Return all the keys that exist
	public Set<?> getKeys() {
		return properties.keySet();
	}
	
	// Check if the key no value
	public boolean isEmpty(String key) {
		if (properties.get(key).toString().length() == 0) {
			return true;
		}
		return false;
	}
	
	// Increment the key by 1, only for integers
	public void inc(String key) {
		properties.put(key, String.valueOf(Integer.parseInt((String)properties.get(key))+1));
	}
	
	// Remove key from map
	public boolean remove(String key) {
		if (properties.remove(key) != null) {
			return true;
		}
		return false;
	}
	
	// get and set property value as a string
	public String getString(String key) {
		if (properties.containsKey(key)) {
			return (String)properties.get(key);
		}
		return "";
	}
	public void setString(String key, String value) {
		properties.put(key, value);
	}
	
	// get and set property value as an int
	public int getInt(String key) {
		if (properties.containsKey(key)) {
			return Integer.parseInt((String)properties.get(key));
		}
		return 0;
	}
	public void setInt(String key, int value) {
		properties.put(key, String.valueOf(value));
		save();
	}
	
	// get and set property value as a double
	public double getDouble(String key) {
		if (properties.containsKey(key)) {
			return Double.parseDouble((String)properties.get(key));
		}
		return 0.0D;
	}
	public void setDouble(String key, double value) {
		properties.put(key, String.valueOf(value));
	}
	
	// get and set property value as a long
	public long getLong(String key) {
		if (properties.containsKey(key)) {
			return Long.parseLong((String)properties.get(key));
		}
		return 0L;
	}
	public void setLong(String key, long value) {
		properties.put(key, String.valueOf(value));
	}
	
	// get and set property value as a float
	public float getFloat(String key) {
		if (properties.containsKey(key)) {
			return Float.parseFloat((String)properties.get(key));
		}
		return 0F;
	}
	public void setFloat(String key, float value) {
		properties.put(key, String.valueOf(value));
	}
	
	// get and set property value as a boolean
	public boolean getBoolean(String key) {
		if (properties.containsKey(key)) {
			return Boolean.parseBoolean((String)properties.get(key));
		}
		return false;
	}
	public void setBoolean(String key, boolean value) {
		properties.put(key, String.valueOf(value));
	}
	
	private String escape(String key) {
		key.replace("=", "\\=");
		key.replace(":", "\\:");
		key.replace(" ", "\\ ");
		return key;
	}
	
	private String unescape(String key) {
		key.replace("\\=", "=");
		key.replace("\\:", ":");
		key.replace("\\ ", " ");
		return key;
	}
}