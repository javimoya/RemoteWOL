package com.javimoya.remotewol.android.server;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MyBroadcastReceiver extends ParsePushBroadcastReceiver {

    private static Context mContext;

/*
    {"action":"app.wolmynas.intent.WOL",
            "ipStr":"192.168.0.255",
            "macStr":"00:08:9B:E6:99:E8"}
    */

    private static byte[] getMacBytes(Context context, String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException(context.getResources().getString(R.string.invalid_mac_address));
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(context.getResources().getString(R.string.invalid_hex_digit_in_mac_address));
        }
        return bytes;
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Bundle extras = intent.getExtras();
            String message = extras != null ? extras.getString("com.parse.Data") : "";
            JSONObject jObject;

            jObject = new JSONObject(message);

            String ipStr = jObject.getString("ipStr");
            String macStr = jObject.getString("macStr");
            int portInt = jObject.getInt("portInt");

            byte[] macBytes = getMacBytes(context, macStr);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(ipStr);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, portInt);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

            Toast.makeText(context, context.getResources().getString(R.string.wol_packet_sent,ipStr,macStr,portInt), Toast.LENGTH_LONG).show();

            //System.out.println("Wake-on-LAN packet sent.");
        }
        catch (Exception e) {

            Toast.makeText(context, context.getResources().getString(R.string.wol_packet_failed,e.toString()), Toast.LENGTH_LONG).show();

        }
        super.onPushReceive(context, intent);
    }
}