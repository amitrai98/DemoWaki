package com.android.amitrai.demowaki;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.net.Socket;

public class MediaStreamClient {
	static final int frequency = 44100;
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    boolean isPlaying;
	int playBufSize;
    Socket socket;
	AudioTrack audioTrack;
	
	public MediaStreamClient(final Context ctx, final String ip, final int port) {
//		if(ip.equalsIgnoreCase(MainActivity.MYIP))
//			return;
		playBufSize= AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
//		audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
		audioTrack.setStereoVolume(1f, 1f);
		
		new Thread() {
			byte[] buffer = new byte[playBufSize];
			public void run() {
				try { socket = new Socket(ip, port); }
				catch (Exception e) {
					e.printStackTrace();
					Intent intent = new Intent()
						.setAction("tw.rascov.MediaStreamer.ERROR")
						.putExtra("msg", e.toString());
					ctx.sendBroadcast(intent);
					return;
				}


				audioTrack.play();
				isPlaying = true;
		        while (isPlaying) {
		        	int readSize = 0;
		            try { readSize = socket.getInputStream().read(buffer); }
		            catch (Exception e) {
	            		e.printStackTrace();
						Intent intent = new Intent()
							.setAction("tw.rascov.MediaStreamer.ERROR")
							.putExtra("msg", e.toString());
						ctx.sendBroadcast(intent);
						break;
					}
		        	audioTrack.write(buffer, 0, readSize);
		        }  
		        audioTrack.stop();
				try { socket.close(); }
				catch (Exception e) { e.printStackTrace(); }
			}
		}.start();
	}
	
	public void stop() {
		isPlaying = false;
	}
	
	public void setVolume(float lvol, float rvol) {
		audioTrack.setStereoVolume(lvol, rvol);
	}
}