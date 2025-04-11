package io.github.tcagmdev.statesmith;

public class NoTargetNodeException extends RuntimeException {
	public NoTargetNodeException(String message) {
		super(message);
	}
    public NoTargetNodeException() {super();}
}
