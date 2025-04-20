package io.github.tcagmdev.statesmith;

import java.util.*;
import java.util.function.Consumer;

public class StateMachine<T> {
	@FunctionalInterface
	public interface OnChangeHandler<T> {
		void handle(StateNode<T> prevNode, StateNode<T> nextNode, T value);
	}

	private final Set<StateNode<T>> nodes = new HashSet<>();
	private StateNode<T> currentNode;
	private OnChangeHandler<T> onChange;

	public StateMachine() {}

	public StateNode<T> addNode(Consumer<T> onEnter, Consumer<T> onExit) {
		StateNode<T> node = new StateNode<>(this, onEnter, onExit);

		this.nodes.add(node);

		return node;
	}
	public StateNode<T> addNode(Consumer<T> onEnter) {
		StateNode<T> node = new StateNode<>(this, onEnter);

		this.nodes.add(node);

		return node;
	}
	public StateNode<T> addNode() {
		StateNode<T> node = new StateNode<>(this);

		this.nodes.add(node);

		return node;
	}

	public void consume(T value) {
		if (this.currentNode == null) throw new IllegalStateException("Attempt to consume a state machine that has no current node");

		StateNode.Target<T> nextNode = this.currentNode.consume(value);

		if (nextNode == null) throw new NoTargetNodeException();

		this.setCurrentNode(nextNode, value);
	}

	public void setCurrentNode(StateNode<T> node, T value) {
		if (node != this.currentNode) {
			if (this.currentNode != null) this.currentNode.triggerOnExit(value);
			if (this.onChange != null) this.onChange.handle(this.currentNode, node, value);
			this.currentNode = node;
			node.triggerOnEnter(value);
		}
	}
	public void setCurrentNode(StateNode.Target<T> target, T value) {
		this.setCurrentNode(target.getTargetNode(value));
		target.triggerOnTransition(value);
	}
	public void setCurrentNode(StateNode<T> node) {
		this.setCurrentNode(node, null);
	}
	public void setCurrentNode(StateNode.Target<T> target) {
		this.setCurrentNode(target, null);
	}

	public boolean currentNodeIs(StateNode<T> node) {
		return this.currentNode.equals(node);
	}

	public void setOnChange(OnChangeHandler<T> handler) {
		this.onChange = handler;
	}
}