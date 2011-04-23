package com.sparkedia.valrix.netstats;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

public class Downloader {
	// Class to download files to the /lib/ folder
	public Downloader(String url, String file, Netstats plugin) {
		try {
			URLConnection con = (new URL(url)).openConnection();
			con.setUseCaches(false);
			InputStream in = con.getInputStream();
			OutputStream out = new FileOutputStream(plugin.getCanonFile(plugin.lib+'/'+file));
			byte[] b = new byte[0x10000];
			int cc = 0;
			int count = 0;
			plugin.log.info('['+plugin.pName+"]: Couldn't find database connector. Downloading now...");
			while (true) {
				count = in.read(b);
				if (count < 0) break;
				out.write(b, 0, count);
				cc += count;
			}
			plugin.log.info('['+plugin.pName+"]: Done downloading connector.");
			in.close();
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			plugin.log.log(Level.SEVERE, '['+plugin.pName+"]: Couldn't find file. Error: ", e);
		} catch (IOException e) {
			plugin.log.log(Level.SEVERE, '['+plugin.pName+"]: Error saving the database connector: ", e);
		}
	}
}
