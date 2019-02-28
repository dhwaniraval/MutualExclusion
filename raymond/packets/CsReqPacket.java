package raymond.packets;

import raymond.utlity.Definitions;

/**
 * @author Dhwani Raval, Vincy Shrine
 *
 *         A CSRequest Packet
 *
 */
public class CsReqPacket implements Packet {

	private static final long serialVersionUID = 1L;
	private String csRequestString;
	private int messageCount;
	private Integer senderId;
	private int csReqId;

	private static int counter;

	public CsReqPacket() {
		counter = 1;
		csReqId = counter;
		messageCount = 0;
		senderId = Definitions.PID;
	}

	public void setSenderId(Integer senderId) {
		this.senderId = senderId;
	}

	public Integer getSenderId() {
		return senderId;
	}

	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}

	public String getCsRequestString() {
		return csRequestString;
	}

	public void setCsRequestString(String csRequestString) {
		this.csRequestString = csRequestString;
	}

	@Override
	public String toString() {
		return "CS_REQ [sender=" + senderId + "req#=" + csReqId + "]";
	}

	public int getCsReqId() {
		return csReqId;
	}

}
