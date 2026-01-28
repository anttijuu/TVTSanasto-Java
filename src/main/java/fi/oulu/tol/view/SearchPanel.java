package fi.oulu.tol.view;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.util.ResourceBundle;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.TermProvider;
import fi.oulu.tol.model.TermProviderObserver;

public class SearchPanel extends JPanel implements fi.oulu.tol.model.TermProviderObserver {

	private JTextField searchField;
	private JLabel searchLabel;
	private JButton clearButton;

	public SearchPanel(TermProvider provider) {
		super();
		provider.addObserver(this);
		ResourceBundle messages = ResourceBundle.getBundle("SearchPanelBundle", Settings.currentLocale());
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		searchLabel = new JLabel(messages.getString("search_lbl"));
		searchField = new JTextField();
		searchField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				provider.setSearchFilter(searchField.getText().trim().toLowerCase());
			}
		});
		searchField.setToolTipText(messages.getString("search_tip"));
		searchField.setPreferredSize(new Dimension(Settings.WINDOW_WIDTH - 100, 16));
		clearButton = new JButton(messages.getString("clear_btn"));
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchField.setText("");
				provider.setSearchFilter("");
			}
		});
		clearButton.setEnabled(false);
		add(searchLabel);
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

	@Override
	public void changeEvent(TermProviderObserver.Topic topic) {
		if (topic == Topic.LANGUAGE_CHANGED) {
			ResourceBundle messages = ResourceBundle.getBundle("SearchPanelBundle", Settings.currentLocale());
			searchField.setToolTipText(messages.getString("search_tip"));
			searchLabel.setText(messages.getString("search_lbl"));
			clearButton.setText(messages.getString("clear_btn"));
		}
	}
}
