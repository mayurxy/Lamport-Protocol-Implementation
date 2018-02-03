import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ConfigRead {
	static int numNodes = 0;
	File file = null;
	static int dMean, cMean, numOfReq;
	static int myNodeId;
	static String myHostName;
	static int myPortNo;
	BufferedWriter output = null;
	static ArrayList<Node> nodes = new ArrayList<>();

	public ConfigRead() {

	}

	// All Print Method
	public static void println(String output) {
		System.out.println(output);
	}

	public static void print(String output) {
		System.out.print(output);
	}

	/**
	 * Parse the config file to get global parameters
	 * 
	 * @param filename
	 *            : Config file
	 * @param myNode
	 *            : id of my node
	 */
	public static void parseConfig(String filename, int myNode) {

		int lineFlag = 1;
		int count = 0;
		int firstLine = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0)
					continue;
				if (!line.startsWith("#")) {
					String tmpWithHash[] = line.split("#.*$");
					String temp[] = tmpWithHash[0].split("\\s+");
					// Reading the fist Line starts
					if (firstLine == 0) {
						numNodes = Integer.parseInt(temp[0]);
						dMean = Integer.parseInt(temp[1]);
						cMean = Integer.parseInt(temp[2]);
						numOfReq = Integer.parseInt(temp[3]);
						// assigning finish, move to next line
						firstLine = 1;
					}
					if ((lineFlag == 1) && temp.length != 3)
						continue;
					if ((lineFlag == 1) && count < numNodes) {
						if (myNode == Integer.parseInt(temp[0])) {
							myNodeId = Integer.parseInt(temp[0]);
							myHostName = temp[1];
							myPortNo = Integer.parseInt(temp[2]);
						}
						nodes.add(new Node(Integer.parseInt(temp[0]), temp[1], Integer.parseInt(temp[2]),
								getKeySet(myNode)));
						count++;
					}
				}
			}

			br.close();
			// printing config parameters
			/*
			 * 
			 * */

			println("Printing Config Parameters \n ");
			println("Number Of Nodes=" + numNodes + "\n" + "Mean For D in miliseconds=" + dMean + "\n"
					+ "Mean For C in miliseconds=" + cMean + "\n" + "Number Of Request=" + numOfReq + "\n");
			println("\nPrinting Nodes Details \n");
			for (Node z : nodes) {
				println("Node Id: " + z.nodeId + " \t Node Hostname :" + z.hostname + "\t Node Port: " + z.port);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * creating a hashmap for each node. It will have keys of all the processes
	 * that come after it
	 * 
	 * @param myNode
	 *            : my node id
	 * @return : boolean hashmap
	 */
	public static HashMap<Integer, Boolean> getKeySet(int myNode) {
		HashMap<Integer, Boolean> hashmap = new HashMap<>();
		for (int i = 0; i < numNodes; i++) {
			if (i > ConfigRead.myNodeId)
				hashmap.put(i, true);
			else if (i < ConfigRead.myNodeId)
				hashmap.put(i, false);
		}
		return hashmap;
	}
}
