import java.io.Serializable;

public class TimeStamp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	long time;
	int nodeId;

	public TimeStamp(long time, int nodeId) {
		this.time = time;
		this.nodeId = nodeId;
	}

	public static long returnTime() {
		java.util.Date date = new java.util.Date();
		return date.getTime();
	}

}
