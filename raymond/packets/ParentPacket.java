package raymond.packets;

import java.util.Map;

/**
 * @author Dhwani Raval, Vincy Shrine
 *
 */
public class ParentPacket implements Packet {

	private static final long serialVersionUID = 1L;

	private Node node;
	private Map<Integer, Node> allProcessMap;

	public ParentPacket(Node node, Map<Integer, Node> nodesAndIdMap) {
		super();
		this.node = node;
		this.allProcessMap = nodesAndIdMap;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	@Override
	public String toString() {
		return "NEIGHBORS [" + node + "]";
	}

	public Map<Integer, Node> getAllProcessMap() {
		return allProcessMap;
	}

}
