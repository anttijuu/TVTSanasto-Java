package fi.oulu.tol.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.Desktop;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import java.util.ResourceBundle;

import com.github.rjeschke.txtmark.Processor;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.Term;
import fi.oulu.tol.model.TermCategory;
import fi.oulu.tol.model.TermProvider;
import fi.oulu.tol.model.TermProviderObserver;

public class TermDetailView extends JPanel implements TermProviderObserver {

	private transient TermProvider provider;
	private transient Term term;
	private transient TermCategory category;
	private JLabel labelCategory;
	private JLabel labelFinnish;
	private JLabel labelEnglish;
	private JEditorPane labelDefinition;
	private JLabel labelLinks;
	private JButton labelURLFinnish;
	private JButton labelURLEnglish;

	private transient URLActionListener urlActionListener = new URLActionListener();

	public TermDetailView(TermProvider provider) {
		this.provider = provider;
		provider.addObserver(this);
		this.term = provider.getSelectedTerm();
		this.category = provider.getSelectedCategory();
		setPreferredSize(new Dimension(Settings.WINDOW_WIDTH - (Settings.LIST_WIDTH * 2), Settings.WINDOW_HEIGHT));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setAlignmentX(LEFT_ALIGNMENT);
		labelCategory = new JLabel("Valitse termi listalta");
		labelCategory.setAlignmentX(LEFT_ALIGNMENT);
		add(labelCategory);
		add(Box.createRigidArea(new Dimension(0, 10)));
		JSeparator separator = new JSeparator();
		add(separator);
		
		Icon flagFi = new ImageIcon(ClassLoader.getSystemResource("images/fi.png"));
		Icon flagEn = new ImageIcon(ClassLoader.getSystemResource("images/en.png"));
		
		labelEnglish = new JLabel(flagEn, SwingConstants.LEADING);
		labelEnglish.setAlignmentX(LEFT_ALIGNMENT);
		add(labelEnglish);
		labelFinnish = new JLabel(flagFi, SwingConstants.LEADING);
		labelFinnish.setAlignmentX(LEFT_ALIGNMENT);
		add(labelFinnish);
		add(Box.createRigidArea(new Dimension(0, 10)));
		labelDefinition = new JEditorPane();
		labelDefinition.setContentType("text/html");
		labelDefinition.setEditable(false);
		labelDefinition.setAlignmentX(LEFT_ALIGNMENT);
		add(labelDefinition);
		labelLinks = new JLabel("Lähteet");
		add(labelLinks);
		add(new JSeparator());
		labelURLEnglish = new JButton();
		labelURLEnglish.setAlignmentX(LEFT_ALIGNMENT);
		labelURLEnglish.addActionListener(urlActionListener);
		add(labelURLEnglish);
		labelURLFinnish = new JButton();
		labelURLFinnish.setAlignmentX(LEFT_ALIGNMENT);
		labelURLFinnish.addActionListener(urlActionListener);
		add(labelURLFinnish);
		updateContents();
	}

	private class URLActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			String URL = button.getText();
			if (URL.startsWith("http")) {
				open(URL);
			}
		}
	}

	private static void open(String uri) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(uri));
			} catch (IOException e) {

			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		} else {

		}
	}

	@Override
	public void changeEvent(Topic topic) {
		if (topic == Topic.SELECTED_CATEGORY_CHANGED) {
			category = provider.getSelectedCategory();
			term = null;
			updateContents();
		} else if (topic == Topic.SELECTED_TERM_CHANGED) {
			term = provider.getSelectedTerm();
			updateContents();
		} else if (topic == Topic.LANGUAGE_CHANGED) {
			updateContents();
		}
	}

	private void updateContents() {
		ResourceBundle messages = ResourceBundle.getBundle("TermDetailViewBundle", Settings.currentLocale());
		labelLinks.setText(messages.getString("sources_lbl"));
		if (null == category || null == term) {
			labelCategory.setText(messages.getString("unselected_term_hint"));
			labelEnglish.setText("");
			labelFinnish.setText("");
			labelDefinition.setText("");
			labelURLEnglish.setText("");
			labelURLFinnish.setText("");
			return;
		}
		labelCategory.setText(category.toString());
		labelEnglish.setText(term.getEnglish());
		labelFinnish.setText(term.getFinnish());
		String result = Processor.process(term.getDefinition());
		labelDefinition.setText(result);
		String link = term.getEnglishLink();
		int linkCount = 0;
		if (link.startsWith("http")) {
			labelURLEnglish.setText(link);
			labelURLEnglish.setVisible(true);
			linkCount++;
		} else {
			labelURLEnglish.setVisible(false);
		}
		link = term.getFinnishLink();
		if (link.startsWith("http")) {
			labelURLFinnish.setText(link);
			labelURLFinnish.setVisible(true);
			linkCount++;
		} else {
			labelURLFinnish.setVisible(false);
		}
		labelLinks.setVisible(linkCount > 0);
	}

}
