package raymond.packets;

import raymond.utlity.Definitions;

/**
 * @author Dhwani Raval, Vincy Shrine
 * 
 *         A Token Packet
 */
public class TokenPacket implements Packet {

	private static final long serialVersionUID = 1L;

	private Integer senderId = Definitions.PID;
	private boolean isSenderToBeAddedToQ = false;

	private CsReqPacket csRequest, nextCsRequest;

	public TokenPacket() {
	}

	public CsReqPacket getNextCsRequest() {
		return nextCsRequest;
	}

	public void setNextCsRequest(CsReqPacket nextCsRequest) {
		this.nextCsRequest = nextCsRequest;
	}

	public Integer getSenderId() {
		return senderId;
	}

	public void setSenderId(Integer id) {
		senderId = id;
	}

	@Override
	public String toString() {
		return "TOKEN [sender=" + senderId + " isSenderToBeAddedToQ= " + isSenderToBeAddedToQ + "]";
	}

	public boolean isSenderToBeAddedToQ() {
		return isSenderToBeAddedToQ;
	}

	public void setSenderToBeAddedToQ(boolean isSenderToBeAddedToQ) {
		this.isSenderToBeAddedToQ = isSenderToBeAddedToQ;
	}

	public CsReqPacket getCsRequest() {
		return csRequest;
	}

	public void setCsRequest(CsReqPacket csRequest) {
		this.csRequest = csRequest;
	}
}
