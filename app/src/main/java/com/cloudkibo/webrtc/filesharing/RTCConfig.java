package com.cloudkibo.webrtc.filesharing;

import java.util.LinkedList;
import java.util.Random;

import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;

public class RTCConfig {

	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	static Random rnd = new Random();
	
	public static LinkedList<PeerConnection.IceServer> getIceServer(){
		// Initialize ICE server list
		LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
		iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
		iceServers.add(new PeerConnection.IceServer("turn:45.55.232.65:3478?transport=udp",
				"cloudkibo", "cloudkibo"));
		iceServers.add(new PeerConnection.IceServer("turn:45.55.232.65:3478?transport=tcp",
				"cloudkibo", "cloudkibo"));
		iceServers.add(new PeerConnection.IceServer("turn:turn.bistri.com:80?transport=udp",
				"homeo", "homeo"));
		iceServers.add(new PeerConnection.IceServer("turn:turn.bistri.com:80?transport=tcp",
				"homeo", "homeo"));
		iceServers.add(new PeerConnection.IceServer("turn:turn.anyfirewall.com:443?transport=tcp",
				"webrtc", "webrtc"));
		iceServers.add(new PeerConnection.IceServer("stun:stun.anyfirewall.com:3478"));
		
		return iceServers;
	}
	
	public static MediaConstraints getMediaConstraints(){
		// Initialize PeerConnection
		MediaConstraints pcMediaConstraints = new MediaConstraints();
		pcMediaConstraints.optional.add(new MediaConstraints.KeyValuePair(
			"DtlsSrtpKeyAgreement", "true"));
		
		return pcMediaConstraints;
	}

	public static MediaConstraints getSDPConstraints(){
		// Initialize PeerConnection
		MediaConstraints pcMediaConstraints = new MediaConstraints();
		pcMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
				"OfferToReceiveAudio", "true"));
		pcMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
				"OfferToReceiveVideo", "true"));

		return pcMediaConstraints;
	}

	public static String randomString( int len ){
		StringBuilder sb = new StringBuilder( len );
		for( int i = 0; i < len; i++ )
			sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
		return sb.toString();
	}

}
