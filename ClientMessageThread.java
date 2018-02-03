import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientMessageThread implements Runnable {

	String ip;
	int port, myNodeId;
	Message message;
	int otherNode;

	/**
	 * @param myNodeId
	 *            : my node id
	 * @param ip
	 *            : host name of other node
	 * @param port
	 *            : port number of other node
	 * @param message
	 *            : message being sent to other node
	 */
	public ClientMessageThread(int myNodeId, String ip, int port, Message message, int otherNode) {
		// System.out.println("Creating a new client thread on node " +
		// myNodeId);
		this.myNodeId = myNodeId;
		this.ip = ip;
		this.port = port;
		this.message = message;
		this.otherNode = otherNode;
	}

	/**
	 * code for sending message through output stream
	 */
	@Override
	public void run() {
		try {
			// create socket with other nodes port number and ip
			Socket s = new Socket(ip, port);
			// // create an output stream for this socket
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			// // send message using the output stream
			oos.writeObject(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
