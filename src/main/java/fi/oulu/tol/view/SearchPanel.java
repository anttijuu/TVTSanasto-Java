package fi.oulu.tol.view;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.TermProvider;

public class SearchPanel extends JPanel {

	public SearchPanel(TermProvider provider) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JTextField searchField = new JTextField();
		searchField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				provider.setSearchFilter(searchField.getText().trim().toLowerCase());
			}
		});
		searchField.setToolTipText("Etsi termejä");
		searchField.setPreferredSize(new Dimension(Settings.WINDOW_WIDTH - 100, 16));
		JButton clearButton = new JButton("Tyhjennä");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchField.setText("");
				provider.setSearchFilter("");
			}
		});
		clearButton.setEnabled(false);
		add(searchField);
		add(clearButton);
		DocumentListener dl = new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateFieldState();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateFieldState();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateFieldState();
			}
			protected void updateFieldState() {
				clearButton.setEnabled(searchField.getText().length() > 0);
			}
		};
		searchField.getDocument().addDocumentListener(dl);
	}

}
