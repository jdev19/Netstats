package com.sparkedia.valrix.netstats;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

public class NetUpdater {
	public NetUpdater(Netstats plugin) {
		try {
			URLConnection con = (new URL("")).openConnection();
			con.setUseCaches(false);
			InputStream in = con.getInputStream();
			OutputStream out = new FileOutputStream(plugin.uf+"/Netstats.jar");
			byte[] b = new byte[0x10000];
			int cc = 0;
			int count = 0;
			while (true) {
				count = in.read(b);
				if (count < 0) break;
				out.write(b, 0, count);
				cc += count;
			}
			plugin.log.info('['+plugin.pName+"]: Newest version of Netstats downloaded.");
			in.close();
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			plugin.log.log(Level.SEVERE, '['+plugin.pName+"]: Couldn't find file. Error: ", e);
		} catch (IOException e) {
			plugin.log.log(Level.SEVERE, '['+plugin.pName+"]: Error saving the file: ", e);
		}
	}
}
