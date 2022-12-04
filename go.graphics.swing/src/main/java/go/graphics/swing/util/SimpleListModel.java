package go.graphics.swing.util;

import java.util.List;
import javax.swing.AbstractListModel;

public class SimpleListModel<E> extends AbstractListModel<E> {

	private final List<E> list;

	public SimpleListModel(List<E> list) {
		this.list = list;
	}

	@Override
	public int getSize() {
		return list.size();
	}

	@Override
	public E getElementAt(int index) {
		return list.get(index);
	}
}
