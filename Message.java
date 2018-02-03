import java.io.Serializable;

enum MessageType {
	RequestMsg, ReplyMsg, ReleaseMsg, TerminationMsg, TestMsg
};

public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String s;
	MessageType messageType;
	TimeStamp ts;

	public Message(String s, MessageType messageType, TimeStamp ts) {
		this.s = s;
		this.messageType = messageType;
		this.ts = ts;
	}

}
