package com.sparkedia.valrix.netstats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class NetServer extends Thread {
	private ServerSocket server;
	private Netstats plugin;

	public NetServer(Netstats plugin) {
		this.plugin = plugin;
		try {
			server = new ServerSocket(9999);
		} catch (IOException e) {
			e.printStackTrace();
		}
		plugin.log.info('['+plugin.pName+"] daemon listening on "+server.getInetAddress()+':'+server.getLocalPort());
		this.setDaemon(true);
		this.start();
	}
	
	public void run() {
		Socket client = null;
		try {
			while (true) {
				client = server.accept();
				BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
				String[] in = br.readLine().split(":");
				br.close();
				if (in[0].equals("ping")) {
					bw.write("ping");
					bw.close();
					client.close();
					continue;
				}
				if (in[0].equals('r')) {
					// PHP sent a request
					String reply = "";
					if (in[1].equals("all")) {
						// Asking for data from each player
						for (File f : new File(plugin.players).listFiles()) {
							Property p = new Property(f.toString(), plugin);
							reply = f.toString().split(".")[0]+':';
							for (Object v : p.getValues()) {
								reply += v.toString()+':';
							}
							reply += ';';
						}
					} else {
						String name = in[1];
						Property p = new Property(plugin.players+'/'+name+".stats", plugin);
						for (Object v : p.getValues()) {
							reply += v.toString()+':';
						}
						reply = name+':'+reply;
					}
					reply.replace(":;", ";");
					// Write back all the data PHP wants
					bw.write(reply);
					bw.close();
					client.close();
					continue;
				}
				if (in[0].equals('p')) {
					// PHP sent a post (command)
					bw.close();
					client.close();
					continue;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
