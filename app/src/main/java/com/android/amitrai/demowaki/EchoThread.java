package com.android.amitrai.demowaki;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import java.net.Socket;

/**
 * Created by amitrai on 28/11/16.
 */

public class EchoThread extends Thread {

    protected Socket socket;

    static final int frequency = 44100;
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    public static boolean isPlaying;
    int playBufSize;
    AudioTrack audioTrack;

    ////////////////////////////////////
    boolean isRecording;
    int recBufSize;
    //    ServerSocket sockfd;
    AudioRecord audioRecord;
    Context ctx;

    public EchoThread(Context ctx, Socket clientSocket) {
        this.socket = clientSocket;
        this.ctx = ctx;
    }

    public void run() {
//        InputStream inp = null;
//        BufferedReader brinp = null;
//        DataOutputStream out = null;
//        try {
//            inp = socket.getInputStream();
//            brinp = new BufferedReader(new InputStreamReader(inp));
//            out = new DataOutputStream(socket.getOutputStream());
//        } catch (IOException e) {
//            return;
//        }
//        String line;
//        while (true) {
//            try {
//                line = brinp.readLine();
//                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
//                    socket.close();
//                    return;
//                } else {
//                    out.writeBytes(line + "\n\r");
//                    out.flush();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                return;
//            }
//        }

        playStream(ctx, socket);
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
