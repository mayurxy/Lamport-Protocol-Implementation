import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.TreeMap;

public class Test {

	public static void main(String[] args) {

		try {
			TreeMap<Long, String> logValues = new TreeMap<>();
			// Parsing log file and storing values in a treemap
			File logFile = new File("node.log");
			Scanner in = new Scanner(logFile);
			while (in.hasNext()) {
				String line = in.next();
				if (line.isEmpty())
					continue;
				String[] fields = line.split(":");
				logValues.put(Long.valueOf(fields[0]), fields[1]);
			}
			in.close();

			/*
			 * Code for testing our implementation
			 */
			String previousValue = null;
			int lineCounter = 1;
			for (String value : logValues.values()) {
				String[] currentLine;
				if (previousValue != null && lineCounter % 2 == 0) {
					currentLine = value.split("-");
					String[] previousLine = previousValue.split("-");
					if (previousLine[0].equals(currentLine[0])) {
						if (previousLine[1].equalsIgnoreCase("Start") && currentLine[1].equalsIgnoreCase("End")) {
							previousValue = null;
							lineCounter++;
							continue;
						}
					} else if ((previousLine[1].equalsIgnoreCase("Start") && currentLine[1].equalsIgnoreCase("Start"))
							|| (previousLine[1].equalsIgnoreCase("End") && currentLine[1].equalsIgnoreCase("End"))) {
						System.out.println("Program is violating the protocol");
						return;
					} else if (previousLine[1].equalsIgnoreCase("Start") && currentLine[1].equalsIgnoreCase("End")) {
						System.out.println("Program is violating the protocol");
						return;
					}
				}
				previousValue = value;
				lineCounter++;
			}
			System.out.println("The protocol is working fine");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
