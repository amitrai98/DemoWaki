package com.android.amitrai.demowaki;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import java.net.Socket;

public class MediaStreamClient {
    static final int frequency = 44100;
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    boolean isPlaying;
    int playBufSize;
    Socket connfd;
    AudioTrack audioTrack;

    ////////////////////////////////////
    public static boolean isRecording;
    int recBufSize;
//    ServerSocket sockfd;
    AudioRecord audioRecord;

    public MediaStreamClient(final Context ctx, final String ip, final int port) {


        new Thread() {
            public void run() {
                try {
                    connfd = new Socket(ip, port);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Intent intent = new Intent()
                            .setAction("tw.rascov.MediaStreamer.ERROR")
                            .putExtra("msg", e.toString());
                    ctx.sendBroadcast(intent);
                    return;
                }

                playBufSize= AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
                audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
                audioTrack.setStereoVolume(1f, 1f);
                recordStream(ctx, connfd);
//                audioTrack.play();
//                isPlaying = true;
//                while (isPlaying) {
//                    int readSize = 0;
//                    try { readSize = connfd.getInputStream().read(buffer); }
//                    catch (Exception e) {
//                        e.printStackTrace();
//                        Intent intent = new Intent()
//                                .setAction("tw.rascov.MediaStreamer.ERROR")
//                                .putExtra("msg", e.toString());
//                        ctx.sendBroadcast(intent);
//                        break;
//                    }
//                    audioTrack.write(buffer, 0, readSize);
//                }
//                audioTrack.stop();
//                try { connfd.close(); }
//                catch (Exception e) { e.printStackTrace(); }
            }
        }.start();
    }

    public void stop() {
        isRecording = false;
    }

    public void setVolume(float lvol, float rvol) {
        audioTrack.setStereoVolume(lvol, rvol);
    }



    private void recordStream(final Context ctx, Socket connfd){
        recBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, recBufSize);
        byte[] buffer = new byte[recBufSize];
        audioRecord.startRecording();
        isRecording = true;
        while (isRecording) {
            EchoThread.isPlaying = false;
            int readSize = audioRecord.read(buffer, 0, recBufSize);
            try { connfd.getOutputStream().write(buffer, 0, readSize); }
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
        try { connfd.close(); }
        catch (Exception e) { e.printStackTrace(); }
    }
}