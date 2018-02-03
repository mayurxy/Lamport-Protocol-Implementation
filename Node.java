import java.util.HashMap;

public class Node {

	int nodeId;
	String hostname;
	int port;
	HashMap<Integer, Boolean> csKey;

	public Node(int nodeId, String hostname, int port, HashMap<Integer, Boolean> csKey) {
		this.nodeId = nodeId;
		this.hostname = hostname;
		this.port = port;
		this.csKey = csKey;
	}

}
