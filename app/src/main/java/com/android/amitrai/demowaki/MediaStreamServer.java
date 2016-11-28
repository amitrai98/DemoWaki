package com.android.amitrai.demowaki;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MediaStreamServer {
	static final int frequency = 44100;
	static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private ServerSocket serverSocket;

	int recBufSize;
	ServerSocket sockfd;
	Socket connfd;
	AudioRecord audioRecord;

	////////////////////////
	boolean isPlaying;
	int playBufSize;
	AudioTrack audioTrack;

	public MediaStreamServer(final Context ctx, final int port) {



		new Thread(new Runnable() {
			public Socket socket;

			@Override
			public void run() {

				try {
					serverSocket = new ServerSocket(port);

				} catch (IOException e) {
					e.printStackTrace();

				}

				while (true) {
					try {
						socket = serverSocket.accept();
					} catch (IOException e) {
						System.out.println("I/O error: " + e);
					}
					// new threa for a client
					if(socket.getRemoteSocketAddress().toString().equalsIgnoreCase(MainActivity.MYIP))
						return;
					new EchoThread(ctx, socket).start();
				}
			}
		}).start();


//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				Socket socket = null;
//
//				try {
//					serverSocket = new ServerSocket(port);
//					while (true) {
//						socket = serverSocket.accept();
//						playStream(ctx, socket);
//					}
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} finally {
//					if (socket != null) {
//						try {
//							socket.close();
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//		}).start();



//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					sockfd = new ServerSocket(port);
//
//					while (true){
//						connfd = sockfd.accept();
//						playStream(ctx, connfd);
//					}
//
//
////					playStream(ctx, connfd);
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//					Intent intent = new Intent()
//							.setAction("tw.rascov.MediaStreamer.ERROR")
//							.putExtra("msg", e.toString());
//					ctx.sendBroadcast(intent);
//					return;
//				}
//			}
//		}).start();


//		recBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
//		byte[] buffer = new byte[recBufSize];
//		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, recBufSize);
//		playStream(ctx, connfd);
		new Thread() {
			byte[] buffer = new byte[recBufSize];
			public void run() {
//				try { connfd = sockfd.accept(); }
//				catch (Exception e) {
//					e.printStackTrace();
//					Intent intent = new Intent()
//							.setAction("tw.rascov.MediaStreamer.ERROR")
//							.putExtra("msg", e.toString());
//					ctx.sendBroadcast(intent);
//					return;
//				}

				playStream(ctx, connfd);
//				audioRecord.startRecording();
//				isRecording = true;
//				while (isRecording) {
//					int readSize = audioRecord.read(buffer, 0, recBufSize);
//					try { connfd.getOutputStream().write(buffer, 0, readSize); }
//					catch (Exception e) {
//						e.printStackTrace();
//						Intent intent = new Intent()
//								.setAction("tw.rascov.MediaStreamer.ERROR")
//								.putExtra("msg", e.toString());
//						ctx.sendBroadcast(intent);
//						break;
//					}
//				}
//				audioRecord.stop();
//				try { connfd.close(); }
//				catch (Exception e) { e.printStackTrace(); }
			}
		};
	}

	public void stop() {
		isPlaying = false;
		try { sockfd.close(); }
		catch (Exception e) { e.printStackTrace(); }
	}


	private void playStream(Context ctx, Socket connfd){

		recBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, recBufSize);

		playBufSize= AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
//		audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
		audioTrack.setStereoVolume(1f, 1f);
		byte[] buffer = new byte[playBufSize];
		audioTrack.play();
                isPlaying = true;
                while (isPlaying) {
                    int readSize = 0;
                    try { readSize = connfd.getInputStream().read(buffer); }
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
                try { connfd.close(); }
                catch (Exception e) { e.printStackTrace(); }
	}
}