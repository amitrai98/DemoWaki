package com.android.amitrai.demowaki;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import java.net.ServerSocket;
import java.net.Socket;

public class MediaStreamServer {
	static final int frequency = 44100;
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    boolean isRecording;
    int recBufSize;
    ServerSocket server_socket;
    Socket socket;
	AudioRecord audioRecord;


	 boolean isPlaying;
	 int playBufSize;
	 AudioTrack audioTrack;
	
	public MediaStreamServer(final Context ctx, final int port) {
		recBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, recBufSize);

		try {
			server_socket = new ServerSocket(port); }
		catch (Exception e) {
			e.printStackTrace();
			Intent intent = new Intent()
					.setAction("tw.rascov.MediaStreamer.ERROR")
					.putExtra("msg", e.toString());
			ctx.sendBroadcast(intent);
			return;
		}

		try { socket = server_socket.accept();
			playAudio(ctx, socket);
		}
		catch (Exception e) {
			e.printStackTrace();
			Intent intent = new Intent()
					.setAction("tw.rascov.MediaStreamer.ERROR")
					.putExtra("msg", e.toString());
			ctx.sendBroadcast(intent);
			return;
		}
		

	}
	
	public void stop() {
		isRecording = false;
		try { server_socket.close(); }
		catch (Exception e) { e.printStackTrace(); }
	}

	/**
	 * starts recording voice
	 */
	private void startRecording(final Context ctx){
		new Thread() {
			byte[] buffer = new byte[recBufSize];
			public void run() {
				audioRecord.startRecording();
				isRecording = true;
				while (isRecording) {
					int readSize = audioRecord.read(buffer, 0, recBufSize);
					try { socket.getOutputStream().write(buffer, 0, readSize); }
					catch (Exception e) {
						e.printStackTrace();
						Intent intent = new Intent()
								.setAction("tw.rascov.MediaStreamer.ERROR")
								.putExtra("msg", e.toString());
						ctx.sendBroadcast(intent);
						break;
					}
				}
				audioRecord.stop();
				try { socket.close(); }
				catch (Exception e) { e.printStackTrace(); }
			}
		}.start();
	}


	private void playAudio(final Context ctx, final Socket socket){

		playBufSize= AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
//		audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
		audioTrack.setStereoVolume(1f, 1f);

		new Thread() {
			byte[] buffer = new byte[playBufSize];
			public void run() {
//				try { socket = server_socket.accept();}
//				catch (Exception e) {
//					e.printStackTrace();
//					Intent intent = new Intent()
//							.setAction("tw.rascov.MediaStreamer.ERROR")
//							.putExtra("msg", e.toString());
//					ctx.sendBroadcast(intent);
//					return;
//				}
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
}