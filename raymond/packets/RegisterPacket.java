package raymond.packets;

/**
 * @author Dhwani Raval, Vincy Shrine
 * 
 *         A Register Packet
 *
 */
public class RegisterPacket implements Packet {

	private static final long serialVersionUID = 1L;

	private String hostName;
	private int processId;
	private int portNo;

	public RegisterPacket() {
	}

	public RegisterPacket(String hostName, int portNo) {
		this.hostName = hostName;
		this.setPortNo(portNo);
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public int getPortNo() {
		return portNo;
	}

	public void setPortNo(int portNo) {
		this.portNo = portNo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + processId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegisterPacket other = (RegisterPacket) obj;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (processId != other.processId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "REGISTER [hostName=" + hostName + ", processId=" + processId + ", portNo=" + portNo + "]";
	}

}
