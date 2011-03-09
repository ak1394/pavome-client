package me.pavo.server;

public class Message {
	public final byte packet;
	public final int id;
	public final Object result;

	public Message(byte packet, int id, Object result) {
		this.packet = packet;
		this.id = id;
		this.result = result;
	}
}
