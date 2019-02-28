package raymond.packets;

import java.io.Serializable;

/**
 * Neighbour Class used for priorityQ to store and update info about neighbor
 * current processing time
 * 
 * @author Dhwani Raval, Vincy Shrine
 *
 */
public class Node implements Serializable {

	private static final long serialVersionUID = 1L;
	private String hostName;
	private int tcpPortNo;
	private int processId;

	public Node(int id, String hostName, int tcpPortNo) {
		this.hostName = hostName;
		this.tcpPortNo = tcpPortNo;
		this.processId = id;
	}

	public Node(int id) {
		this.processId = id;
	}

	public int getPortNo() {
		return tcpPortNo;
	}

	public String getHostName() {
		return hostName;
	}

	public Integer getProcessId() {
		return processId;
	}

	public void setProcessId(int id) {
		this.processId = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + processId;
		result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + tcpPortNo;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (processId != other.processId)
			return false;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (tcpPortNo != other.tcpPortNo)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return processId + "";
	}

}
