package raymond.packets;

import raymond.utlity.Definitions;

/**
 * @author Dhwani Raval, Vincy Shrine
 * 
 *         A Terminate Packet
 *
 */
public class TerminatePacket implements Packet {

	private static final long serialVersionUID = 1L;

	private Integer senderId;

	public TerminatePacket() {
		this.senderId = Definitions.PID;
	}

	public Integer getSenderId() {
		return senderId;
	}

}
