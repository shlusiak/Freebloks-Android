package de.saschahlusiak.freebloks.controller;

public class JNIServer {
	private static native int native_run_server(int ki_mode);
	
	public static void runServer(int ki_mode) {
		native_run_server(ki_mode);
	}
	
	static {
		System.loadLibrary("server");
	}
}
