package raymond.packets;

import raymond.utlity.Definitions;

/**
 * @author Dhwani Raval, Vincy Shrine
 *
 *         A Mutual Exclusion Complete Packet
 *
 */
public class MeCompletePacket implements Packet {

	private static final long serialVersionUID = 1L;

	private Integer senderId;

	public MeCompletePacket() {
		this.senderId = Definitions.PID;
	}

	public Integer getSenderId() {
		return senderId;
	}

}
