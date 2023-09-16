package jsettlers.common.action;
import java.util.List;
import jsettlers.common.selectable.ISelectable;;

/**
 * This class hold special information for the action type {@link EActionType#SELECT_MOVABLES}.
 * 
 * @author binarymaus
 */
public class SelectMovablesAction extends Action {
	private final List<ISelectable> selection;

	/**
	 * Creates a new select movables action.
	 * 
	 * @param selectables
	 *            The selected movables.
	 */
	public SelectMovablesAction(List<ISelectable> selectables) {
		super(EActionType.SELECT_MOVABLES);
		this.selection = selectables;		
	}

	/**
	 * Gets the selected movables.
	 * 
	 * @return The selected movables.
	 */
	public List<ISelectable> getSelection() {
		return this.selection;
	}
}
