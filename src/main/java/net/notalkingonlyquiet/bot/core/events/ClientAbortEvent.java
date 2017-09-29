/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.notalkingonlyquiet.bot.core.events;

/**
 *
 * @author arawson
 */
public class ClientAbortEvent {
	private final String reason;

	public ClientAbortEvent(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}
}
