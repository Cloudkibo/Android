package com.cloudkibo.webrtc.filesharing;

import java.util.LinkedList;

import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;

public class RTCConfig {
	
	public static LinkedList<PeerConnection.IceServer> getIceServer(){
		// Initialize ICE server list
		LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
		iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
		iceServers.add(new PeerConnection.IceServer("turn:cloudkibo@162.243.217.34:3478?transport=udp",
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
		pcMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
				"OfferToReceiveAudio", "true"));
		pcMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
				"OfferToReceiveVideo", "true"));
		
		return pcMediaConstraints;
	}

}
