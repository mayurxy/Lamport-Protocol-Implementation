import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Server extends Thread implements Runnable {
	int myNodeId;
	ArrayList<Node> nodes;
	public static PriorityQueue<Message> queue;
	public static volatile int replyCounter = 0;
	public static volatile boolean replyFromAll = false;
	public static volatile int termination = 0;

	public Server(int myNodeId, ArrayList<Node> nodes) {
		queue = new PriorityQueue<>(11, new Comparator<Message>() {

			@Override
			public int compare(Message o1, Message o2) {
				if (o1.ts.time > o2.ts.time)
					return 1;
				else
					return -1;
			}

		});
		// System.out.println("Initializing server object for node " +
		// myNodeId);
		this.nodes = nodes;
		this.myNodeId = myNodeId;
	}

	public void sendReplyMsg(int nodeId) {
		// System.out.println("Sending reply msg from node " + myNodeId + " to
		// node " + nodeId);
		// host name of other node
		String ip = nodes.get(nodeId).hostname;
		// port num of other node
		int port = nodes.get(nodeId).port;
		// generate timestamp for reply
		// this class is being accessed by two different threads. So it can
		// cause error
		long time = TimeStamp.returnTime();
		Message m = new Message("This is a test message", MessageType.ReplyMsg, new TimeStamp(time, myNodeId));
		// start client to send request msg to other node
		startClient(myNodeId, ip, port, m, nodeId);
	}

	/**
	 * Start a client instance in a new thread
	 * 
	 * @param myNodeId
	 *            : my node id
	 * @param ip
	 *            : host name to other node
	 * @param port
	 *            : port num of other node
	 * @param message
	 *            : message to be sent
	 */
	public void startClient(int myNodeId, String ip, int port, Message message, int otherNode) {
		new Thread(new ClientMessageThread(myNodeId, ip, port, message, otherNode)).start();
	}

	@Override
	public void run() {

		// System.out.println("starting runnable for server of node " +
		// myNodeId
		ServerSocket s = null;
		try {

			s = new ServerSocket(nodes.get(myNodeId).port);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("unbale to create server socket");
			e1.printStackTrace();
		}
		// System.out.println("Created server socket for node " + myNodeId);
		// keep running. accept connections if any other node wants to make
		// channels with this server's socket
		while (true) {
			try {
				// returns a socket. use this socket to do communication between
				// nodes
				Socket socket = s.accept();
				// System.out.println("Accepted socket on node " + myNodeId);
				// this class takes socket as input and does communication with
				// other node in a new thread
				new Thread(new ServerMessageThread(socket, myNodeId, nodes)).start();

			} catch (Exception e) {
				System.out.println(e);
				System.out.println("Problem in reading object at the server");

				e.printStackTrace();
			}

		}

		// System.out.println("Created new thread for communication on
		// node " + myNodeId);
	}

}
