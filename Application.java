import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Random;

public class Application {
	Message temp = null;
	public static HashMap<Integer, Boolean> replyMap;
	static HashMap<Integer, ObjectOutputStream> clientSockets;
	int checkForTop;
	int[] requestDelay;
	int[] cMean;
	PrintWriter out;
	String fileName = "node.log";

	public static void main(String[] args) {
		ConfigRead.parseConfig(args[0], Integer.parseInt(args[1]));
		Application app = new Application();
		clientSockets = new HashMap<Integer, ObjectOutputStream>();

		app.startApplication();
		app.stopApplication();
	}

	public Application() {
		requestDelay = new int[ConfigRead.numOfReq];
		cMean = new int[ConfigRead.numOfReq];
		// created log file
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("File Created");
		out.close();
		// synchronized (replyMap) {
		// replyMap = new HashMap<>();
		// // setting default value of reply's as false
		// for (int i = 0; i < ConfigRead.numNodes; i++) {
		// if (i == ConfigRead.myNodeId)
		// continue;
		// replyMap.put(i, false);
		// }
		// }
	}

	public void startApplication() {

		requestDelay = genNum(ConfigRead.numOfReq, ConfigRead.dMean);
		cMean = genNum(ConfigRead.numOfReq, ConfigRead.cMean);
		// System.out.println("Starting application for node " +
		// ConfigRead.myNodeId);
		Server s = new Server(ConfigRead.myNodeId, ConfigRead.nodes);
		// start running the server thread. This will create sockets of this
		// node. Whenever a message comes from other node, its runnable will
		// accept the connection and make a new thread for communication between
		// the 2 nodes
		s.start();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		/*
		 * Code for completing number of critical section requests
		 */
		for (int i = 0; i < ConfigRead.numOfReq; i++) {
			csEnter();
			csExecute(cMean[i]);
			csExit();
			try {
				Thread.sleep(requestDelay[i]);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public void stopApplication() {
		++Server.termination;
		// generate timestamp for termination
		long time = TimeStamp.returnTime();
		Message m = new Message("This is a request message", MessageType.TerminationMsg,
				new TimeStamp(time, ConfigRead.myNodeId));
		// send termination
		sendTermination(m);
		while (true) {
			if (Server.termination == ConfigRead.numNodes) {
				try {
					System.exit(2);
				} catch (Exception e) {

				}
			}
		}

	}

	/**
	 * Code for entering critical section. method blocks until request is
	 * satisfied
	 */
	public void csEnter() {
		// System.out.println("Starting csEnter");
		// generate timestamp for request
		long time = TimeStamp.returnTime();
		// request message
		Message m = new Message("This is a request message", MessageType.RequestMsg,
				new TimeStamp(time, ConfigRead.myNodeId));
		// add message to its own queue
		synchronized (Server.queue) {
			Server.queue.add(m);
			// System.out.println("added node " + m.ts.nodeId + " to queue of
			// node " + ConfigRead.myNodeId);
			// System.out.println("adding req to queue " + m.ts.nodeId);
			// temp = Server.queue.peek();
			// checkForTop = temp.ts.nodeId;
			// System.out.println("top most req is " + temp.ts.nodeId);
		}
		// node will send request msg to all the other nodes in the system to
		// enter critical section and wait for their reply msg
		sendRequestMsg(m);
		// try {
		// Thread.sleep(2000);
		// } catch (InterruptedException e1) {
		// e1.printStackTrace();
		// }

		while (true) {
			// System.out.println("\n\n\nHello\n\n\n\n");
			try {
				Thread.sleep(100);
				synchronized (Server.queue) {
					// System.out.println("Entering inside synchronized");
					if (Server.replyFromAll && ConfigRead.myNodeId == Server.queue.peek().ts.nodeId) {
						// System.out.println("trying to break");
						break;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// System.out.println("blocking csEnter");
			// System.out.println("Top el of queue is:" +
			// Server.queue.peek().ts.nodeId + "\n and my node id is "
			// + ConfigRead.myNodeId);
			// System.out.println("No. of replies are:" + Server.replyCounter);
			// System.out.println("Reply flag is:" + Server.replyFromAll);
		}

		System.out.println("Entering critical section for node " + ConfigRead.myNodeId);
		// when it gets out of its while loop, it can execute critical section

		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
			out.println();
			out.append(System.currentTimeMillis() + ":" + ConfigRead.myNodeId + "-Start");
			out.println();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		out.close();

	}

	public void csExecute(int cMean) {
		System.out.println("Executing critical section for node " + ConfigRead.myNodeId);
		try {
			Thread.sleep(cMean);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void csExit() {
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
			out.append(System.currentTimeMillis() + ":" + ConfigRead.myNodeId + "-End");
			out.println();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		out.close();
		System.out.println("Leaving critical section for node " + ConfigRead.myNodeId);
		// set reply flag as false again
		Server.replyFromAll = false;
		// flushing replyCounter
		Server.replyCounter = 0;
		// generate timestamp for release
		long time = TimeStamp.returnTime();
		// release message
		Message m = new Message("This is a release message", MessageType.ReleaseMsg,
				new TimeStamp(time, ConfigRead.myNodeId));
		// finally send a release msg to all the nodes
		sendReleaseMsg(m);

	}

	public void sendReleaseMsg(Message m) {
		for (int i = 0; i < ConfigRead.numNodes; i++) {
			if (i == ConfigRead.myNodeId) {
				synchronized (Server.queue) {
					Message temp = Server.queue.poll();
					// System.out.println("Removed node " + temp.ts.nodeId + "
					// from queue of node " + ConfigRead.myNodeId);
				}
				continue;
			}
			// System.out.println("Sending release msg to node " + i + " from
			// node " + ConfigRead.myNodeId);
			// my node id
			// System.out.println("Sending release from node " +
			// ConfigRead.myNodeId + " to node " + i);
			int myNodeId = ConfigRead.myNodeId;
			// host name of other node
			String ip = ConfigRead.nodes.get(i).hostname;
			// port num of other node
			int port = ConfigRead.nodes.get(i).port;
			// start client to send request msg to other node
			startClient(myNodeId, ip, port, m, i);
		}
	}

	/**
	 * Send request message to all the other nodes nodes in the system
	 * 
	 * @param message
	 */
	public void sendRequestMsg(Message message) {
		for (int i = 0; i < ConfigRead.numNodes; i++) {
			if (i == ConfigRead.myNodeId)
				continue;
			// my node id
			// System.out.println("Sending request from node " +
			// ConfigRead.myNodeId + " to node " + i);
			int myNodeId = ConfigRead.myNodeId;
			// host name of other node
			String ip = ConfigRead.nodes.get(i).hostname;
			// port num of other node
			int port = ConfigRead.nodes.get(i).port;
			// start client to send request msg to other node
			startClient(myNodeId, ip, port, message, i);
		}
	}

	public void sendTermination(Message message) {
		for (int i = 0; i < ConfigRead.numNodes; i++) {
			if (i != ConfigRead.myNodeId) {
				int myNodeId = ConfigRead.myNodeId;
				// host name of other node
				String ip = ConfigRead.nodes.get(i).hostname;
				// port num of other node
				int port = ConfigRead.nodes.get(i).port;
				// start client to send request msg to other node
				startClient(myNodeId, ip, port, message, i);
			}
		}

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

	/**
	 * code for generating exponential values
	 * 
	 * @param n
	 *            : num of requests
	 * @param c
	 *            : mean value of exponential curve
	 * @return
	 */
	public static int[] genNum(int n, int c) {

		int i;
		// n=10;
		// c = 10;
		Random rand = new Random();
		Random random1 = new Random();

		int[] randomnumarray = new int[n];
		int sum = (n * c) / 2;
		int myrandnum = 0;
		int upperbound = Long.valueOf(Math.round(sum / n)).intValue();
		// System.out.println("upperbound::" + upperbound);
		int offset = Long.valueOf(Math.round((upperbound / 2))).intValue();
		// System.out.println("offset::" + offset);
		int variation = sum;
		int finalsum = 0;
		for (i = 0; i < n; i++) {
			myrandnum = rand.nextInt(upperbound) + offset;
			if (((finalsum + myrandnum) > sum) || (i == (n - 1))) {
				myrandnum = sum - finalsum;
			}
			finalsum = finalsum + myrandnum;
			randomnumarray[i] = myrandnum;

			if (finalsum == sum) {
				break;
			}
		}
		// System.out.println("array after distributed mean delay:::" +
		// Arrays.toString(randomnumarray));

		int[] array1 = new int[randomnumarray.length + 1];
		for (int j = 0; j < randomnumarray.length; j++) {
			array1[j] = randomnumarray[j];
		}
		array1[randomnumarray.length] = variation;

		// System.out.println("array after variation:::" +
		// Arrays.toString(array1));

		for (int k = array1.length - 1; k > 0; k--) {
			int index = random1.nextInt(k + 1);
			// System.out.println("index::" + index);
			int a = array1[index];
			array1[index] = array1[k];
			array1[k] = a;
		}
		// System.out.println("array after shuffle:::" +
		// Arrays.toString(array1));
		return array1;

	}

}