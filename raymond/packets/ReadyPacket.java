package raymond.packets;

import raymond.utlity.Definitions;

/**
 * @author Dhwani Raval, Vincy Shrine
 * 
 *         A Ready Packet
 */
public class ReadyPacket implements Packet {

	private static final long serialVersionUID = 1L;

	private Integer senderId;

	public ReadyPacket() {
		this.senderId = Definitions.PID;
	}

	public Integer getSenderId() {
		return senderId;
	}

}
