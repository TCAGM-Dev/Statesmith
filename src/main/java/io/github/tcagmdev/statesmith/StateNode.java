package io.github.tcagmdev.statesmith;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class StateNode<T> {
	public static class Target<T> {
		private final Function<T, StateNode<T>> targetNodeProvider;
		private final Consumer<T> onTransition;

		private Target(Function<T, StateNode<T>> targetNodeProvider, Consumer<T> onTransition) {
			this.targetNodeProvider = targetNodeProvider;
			this.onTransition = onTransition;
		}

		public StateNode<T> getTargetNode(T input) {
			if (this.targetNodeProvider == null) return null;
			return this.targetNodeProvider.apply(input);
		}
		public void triggerOnTransition(T value) {
			if (this.onTransition != null) this.onTransition.accept(value);
		}
	}

	private final Map<Predicate<T>, Target<T>> targets = new HashMap<>();
	private Target<T> defaultTarget;
	private final StateMachine<T> parent;
	private Consumer<T> onEnter;
	private Consumer<T> onExit;
	private Duration timeoutDuration;
	private Target<T> timeoutTarget;
	private Thread timeoutThread;

	protected StateNode(StateMachine<T> parent, Consumer<T> onEnter, Consumer<T> onExit) {
		this.parent = parent;
		this.onEnter = onEnter;
		this.onExit = onExit;
	}
	protected StateNode(StateMachine<T> parent, Consumer<T> onEnter) {
		this.parent = parent;
		this.onEnter = onEnter;
	}
	protected StateNode(StateMachine<T> parent) {
		this.parent = parent;
	}

	public void setDefaultTarget(StateNode<T> node, Consumer<T> onTransition) {
		this.defaultTarget = new Target<>(_ -> node, onTransition);
	}
	public void setDefaultTarget(StateNode<T> node) {
		this.setDefaultTarget(node, null);
	}

	public StateNode<T> addConnection(Predicate<T> predicate, Target<T> target) {
		this.targets.put(predicate, target);
		return this;
	}
	public StateNode<T> addConnection(Predicate<T> predicate, StateNode<T> targetNode, Consumer<T> onTransition) {
		return this.addConnection(predicate, new Target<>(_ -> targetNode, onTransition));
	}
	public StateNode<T> addConnection(Predicate<T> predicate, StateNode<T> targetNode) {
		return this.addConnection(predicate, targetNode, null);
	}
	public StateNode<T> addConnection(Predicate<T> predicate, Function<T, StateNode<T>> targetNodeProvider, Consumer<T> onTransition) {
		return this.addConnection(predicate, new Target<>(targetNodeProvider, onTransition));
	}
	public StateNode<T> addConnection(Predicate<T> predicate, Function<T, StateNode<T>> targetNodeProvider) {
		return this.addConnection(predicate, targetNodeProvider, null);
	}

	public Target<T> consume(T value) {
		for (Map.Entry<Predicate<T>, Target<T>> connection : this.targets.entrySet()) if (connection.getKey().test(value)) {
			return connection.getValue();
		}
		return this.defaultTarget;
	}

	protected void triggerOnEnter(T value) {
		if (this.onEnter != null) this.onEnter.accept(value);

		if (this.timeoutDuration != null && this.timeoutTarget != null) {
			if (this.timeoutThread != null && !this.timeoutThread.isInterrupted()) this.timeoutThread.interrupt();
			this.timeoutThread = new Thread(() -> {
				try {
					Thread.sleep(this.timeoutDuration);
				} catch (InterruptedException _) {}
				this.parent.setCurrentNode(this.timeoutTarget);
			});
			this.timeoutThread.start();
		}
	}
	protected void triggerOnExit(T value) {
		if (this.onExit != null) this.onExit.accept(value);

		if (this.timeoutThread != null && !this.timeoutThread.isInterrupted()) this.timeoutThread.interrupt();
	}

	public void setTimeoutTarget(Target<T> target, Duration timeoutDuration) {
		this.timeoutTarget = target;
		this.timeoutDuration = timeoutDuration;
	}
	public void setTimeoutTarget(StateNode<T> targetNode, Duration timeoutDuration) {
		this.setTimeoutTarget(new Target<>(_ -> targetNode, null), timeoutDuration);
	}
}