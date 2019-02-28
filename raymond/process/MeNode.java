package raymond.process;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import raymond.packets.CsReqPacket;
import raymond.packets.Node;
import raymond.packets.Packet;
import raymond.packets.TokenPacket;
import raymond.utlity.Definitions;

/**
 * @author Dhwani Raval, Vincy Shrine
 *
 *         A class to store information about process
 * 
 */
public class MeNode implements Serializable {

	private Integer processId;
	private String hostName;
	private Integer tcpPortNo;
	private Map<Integer, ObjectOutputStream> socketStreamMap = java.util.Collections
			.synchronizedMap(new HashMap<>(Definitions.MAX_PROCESS));
	private ExecutorService consumerQExecutor = Executors.newSingleThreadExecutor();

	private Map<Integer, Node> processInformation = new HashMap<>(Definitions.MAX_PROCESS);

	private volatile boolean isTokenWithMe = false;
	private volatile boolean isCsExecutionOngoing = false;

	private List<Integer> RN = new ArrayList<>(Collections.nCopies(Definitions.MAX_PROCESS, 0));
	private TokenPacket token = null;
	private Node parent = null;

	private ArrayBlockingQueue<Packet> csReqQ = new ArrayBlockingQueue<>(Definitions.COMPUTE_Q_SIZE);
	private ConcurrentLinkedQueue<Integer> requestQ = new ConcurrentLinkedQueue<>();
	private ArrayBlockingQueue<TokenPacket> tokenQ = new ArrayBlockingQueue<>(1);

	private static MeNode node;
	private static final long serialVersionUID = 1L;

	private HashMap<String, Integer> successfulCsRequestMap = new HashMap<>();
	private HashMap<Integer, CsReqPacket> csRequestMap = new HashMap<>();

	public HashMap<String, Integer> getSuccessfulCsRequestMap() {
		return successfulCsRequestMap;
	}

	public void addToCsRequestMap(Integer pId, CsReqPacket csRequestPacket) {
		csRequestMap.put(pId, csRequestPacket);
	}

	public void removeFromCsRequestMap(Integer pId) {
		csRequestMap.remove(pId);
	}

	public void incrementMessageCount(Integer pId) {
		CsReqPacket csRequestPacket = csRequestMap.get(pId);
		csRequestPacket.setMessageCount(csRequestMap.get(pId).getMessageCount() + 1);
		csRequestMap.replace(pId, csRequestPacket);
	}

	public CsReqPacket fetchCsRequestPacketFromMap(Integer pId) {
		return csRequestMap.get(pId);
	}

	public void addToSuccessfulCsRequestMap(String csRequest, Integer messageCount) {
		successfulCsRequestMap.put(csRequest, messageCount);
	}

	public int getSuccessfulCsRequestFromMap(String csRequestString) {
		return successfulCsRequestMap.get(csRequestString);
	}

	public static synchronized MeNode getInstance() {
		if (node == null) {
			node = new MeNode();
		}
		return node;
	}

	private MeNode() {
		if (Definitions.AM_I_COORDINATOR)
			isTokenWithMe = true;
	}

	public Integer getId() {
		return processId;
	}

	public void setProcessId(int id) {
		this.processId = id;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String ipAddress) {
		this.hostName = ipAddress;
	}

	public Integer getPortNo() {
		return tcpPortNo;
	}

	public void setPortNo(int tcpPortNo) {
		this.tcpPortNo = tcpPortNo;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node node) {
		this.parent = node;
	}

	public Map<Integer, ObjectOutputStream> getSocketStreamMap() {
		return socketStreamMap;
	}

	public void addToSocketStreamMap(Integer id, ObjectOutputStream stream) {
		this.socketStreamMap.put(id, stream);
	}

	public ArrayBlockingQueue<Packet> getComputeQ() {
		return csReqQ;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + processId;
		result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + tcpPortNo;
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
		MeNode other = (MeNode) obj;
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

	@Override
	public String toString() {
		return "Node [id=" + processId + "parent=" + node.getId() + "]";
	}

	public ExecutorService getConsumerQThread() {
		return consumerQExecutor;
	}

	public boolean isTokenWithMe() {
		return isTokenWithMe;
	}

	public void setTokenWithMe(boolean isTokenWithMe) {
		this.isTokenWithMe = isTokenWithMe;
	}

	public boolean isCsExecutionOngoing() {
		return isCsExecutionOngoing;
	}

	public void setCsExecutionOngoing(boolean isCsExecutionOngoing) {
		this.isCsExecutionOngoing = isCsExecutionOngoing;
	}

	public Integer getRN(int index) {
		return RN.get(index - 1);
	}

	public List<Integer> getRNList() {
		return RN;
	}

	public void setRN(int index, int val) {
		RN.set(index - 1, val);
	}

	public void incrementRN(int index) {
		RN.set(index - 1, RN.get(index - 1) + 1);
		System.out.println("** RN(" + index + ")=" + RN.get(index - 1));
	}

	public ArrayBlockingQueue<TokenPacket> getTokenQ() {
		return tokenQ;
	}

	public TokenPacket getToken() {
		return token;
	}

	public void setToken(TokenPacket token) {
		this.token = token;
	}

	public Map<Integer, Node> getProcessInformation() {
		return processInformation;
	}

	public void setProcessInformation(Map<Integer, Node> processInformation) {
		this.processInformation = processInformation;
	}

	public ConcurrentLinkedQueue<Integer> getRequestQ() {
		return requestQ;
	}

}