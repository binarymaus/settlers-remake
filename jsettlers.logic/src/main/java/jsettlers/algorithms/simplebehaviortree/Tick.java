package jsettlers.algorithms.simplebehaviortree;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Tick<T> {

	public final Root<T> root;
	public final T       target;

	private final Set<Node<T>> openNodes = new HashSet<>();
	private final Map<Integer, Object> properties = new TreeMap<>();

	public Tick(T target, Root<T> root) {
		this.root = root;
		this.target = target;
	}

	public NodeStatus tick() {
		return root.execute(this);
	}

	public void close() {
		root.close(this);
	}

	public boolean isOpen(Node<T> node) {
		return openNodes.contains(node);
	}

	public void visitNode(Node<T> node) {
		openNodes.add(node);
	}

	public void tickNode(Node<T> node) {
	}

	public void leaveNode(Node<T> node) {
		openNodes.remove(node);
	}

	public <I> I getProperty(int id) {
		return (I) properties.get(id);
	}

	public void setProperty(int id, Object value) {
		properties.put(id, value);
	}

	public void serialize(ObjectOutputStream oos) throws IOException {
		oos.writeObject(properties);

		oos.writeInt(openNodes.size());

		for(int openNode : openNodes.stream().map(Node::getId).sorted().toArray(Integer[]::new)) {
			oos.writeInt(openNode);
		}
	}

	public static <T> Tick<T> deserialize(ObjectInputStream ois, T target, Root<T> root)
			throws IOException, ClassNotFoundException {
		Tick<T> out = new Tick<>(target, root);

		out.properties.putAll((Map<Integer, Object>) ois.readObject());
		int openNodeCount = ois.readInt();
		for(int i = 0; i < openNodeCount; i++) {
			Node<T> openNode = root.findNode(ois.readInt());

			if(openNode == null) throw new Error("Unknown open node!");

			out.openNodes.add(openNode);
		}

		return out;
	}
}
