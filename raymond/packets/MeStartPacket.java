package raymond.packets;

import raymond.utlity.Definitions;

/**
 * @author Dhwani Raval, Vincy Shrine
 *
 *         A Mutual Excusion Start Packet
 */
public class MeStartPacket implements Packet {

	private static final long serialVersionUID = 1L;

	private Integer senderId;

	public MeStartPacket() {
		this.senderId = Definitions.PID;
	}

	public Integer getSenderId() {
		return senderId;
	}

}
