package fi.oulu.tol.view;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.github.rjeschke.txtmark.Processor;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.Term;
import fi.oulu.tol.model.TermCategory;
import fi.oulu.tol.model.TermProvider;
import fi.oulu.tol.model.TermProviderObserver;

public class TermDetailView extends JPanel implements TermProviderObserver {

	private TermProvider provider;
	private Term term;
	private TermCategory category;
	private JLabel labelCategory;
	private JLabel labelFinnish;
	private JLabel labelEnglish;
	private JEditorPane labelDefinition;
	private JLabel labelURLFinnish;
	private JLabel labelURLEnglish;

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
		labelEnglish = new JLabel();
		labelEnglish.setAlignmentX(LEFT_ALIGNMENT);
		add(labelEnglish);
		labelFinnish = new JLabel();
		labelFinnish.setAlignmentX(LEFT_ALIGNMENT);
		add(labelFinnish);
		labelDefinition = new JEditorPane();
		labelDefinition.setContentType("text/html");
		labelDefinition.setEditable(false);
		labelDefinition.setAlignmentX(LEFT_ALIGNMENT);
		add(labelDefinition);
		labelURLEnglish = new JLabel();
		labelURLEnglish.setAlignmentX(LEFT_ALIGNMENT);
		add(labelURLEnglish);
		labelURLFinnish = new JLabel();
		labelURLEnglish.setAlignmentX(LEFT_ALIGNMENT);
		add(labelURLFinnish);
		if (term != null) {
			updateContents();
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
		}
	}

	private void updateContents() {
		if (null == category || null == term) {
			labelCategory.setText("Valitse kategoria ja termi listalta");
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
		labelURLEnglish.setText(term.getEnglishLink());
		labelURLFinnish.setText(term.getFinnishLink());
	}
}
