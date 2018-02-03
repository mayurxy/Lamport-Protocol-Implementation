import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ServerMessageThread implements Runnable {

	Socket socket;
	ObjectInputStream inputStream;
	ArrayList<Node> nodes;
	int myNodeId;

	/**
	 * Get input stream using socket
	 * 
	 * @param socket
	 *            : socket that is to be used for communication between two
	 *            nodes
	 */
	public ServerMessageThread(Socket socket, int myNodeId, ArrayList<Node> nodes) {
		// System.out.println("Created message thread class object on node " +
		// myNodeId);
		this.myNodeId = myNodeId;
		this.nodes = nodes;
		this.socket = socket;
		// System.out.println(socket.toString());
		try {
			inputStream = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// System.out.println("Starting runnable for message thread of node " +
		// myNodeId);
		while (true) {
			try {
				Message m = (Message) inputStream.readObject();
				// System.out.println(m.s);
				// System.out.println("\n\n\n\nReceived message on node " +
				// myNodeId + " of type " + m.messageType
				// + " from node" + m.ts.nodeId + "\n\n\n\n\n");
				if (m.messageType == MessageType.RequestMsg) {
					// on receiving a request, add it to its own priority queue
					synchronized (Server.queue) {
						Server.queue.add(m);
						// System.out.println("added node " + m.ts.nodeId + " to
						// queue of node " + myNodeId);
						// System.out.println(
						// "Inside server Message Thread. top el of queue is " +
						// Server.queue.peek().ts.nodeId);
					}
					// send reply message to the node you have received request
					// msg from
					sendReplyMsg(m.ts.nodeId);

				} else if (m.messageType == MessageType.ReplyMsg) {
					// System.out.println("reply to:: " + myNodeId + " from :" +
					// m.ts.nodeId);
					++Server.replyCounter;
					// System.out.println("\n\n**number of replies received::" +
					// Server.replyCounter + "\n\n");
					if (Server.replyCounter == nodes.size() - 1)
						Server.replyFromAll = true;
					// as soon as this node receives as reply msg, it adds it to
					// its replyMap and checks whether it has received reply
					// from all the nodes

				} else if (m.messageType == MessageType.ReleaseMsg) {
					// System.out.println("Received release msg to node " +
					// myNodeId + " from node " + m.ts.nodeId);
					// remove top most message node from queue
					synchronized (Server.queue) {
						Message temp = Server.queue.poll();
						// System.out.println("Removed node " + temp.ts.nodeId +
						// " from queue of node " + myNodeId);

						// System.out.println("after released node to top queue
						// is::" + Server.queue.peek());
					}

				} else if (m.messageType == MessageType.TerminationMsg) {
					++Server.termination;
				} else {
					System.out.println("Congratulations! You have won nobel prize!!");
				}
				// inputStream.close();

			} catch (ClassNotFoundException e) {
				System.out.println("Message class not found");
				e.printStackTrace();
			} catch (IOException e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		}

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

}
