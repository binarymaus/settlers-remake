package jsettlers.mapcreator.main.window.newmap;

import jsettlers.mapcreator.localization.EditorLabels;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public abstract class FilePicker extends Box {

	private final JLabel filename = new JLabel();
	private final JButton dialogButton = new JButton(EditorLabels.getLabel("newfile.select-image"));
	private final JFileChooser chooserDialog = new JFileChooser();

	public FilePicker() {
		super(BoxLayout.X_AXIS);

		add(filename);
		dialogButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int result = chooserDialog.showOpenDialog(FilePicker.this);
				if(result != JFileChooser.APPROVE_OPTION) {
					return;
				}
				File file = chooserDialog.getSelectedFile();
				if(!checkFile(file)) {
					return;
				}

				filename.setText(file.getName());
			}
		});
		add(dialogButton);
	}

	protected abstract boolean checkFile(File file);
}
