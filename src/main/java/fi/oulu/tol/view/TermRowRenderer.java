package fi.oulu.tol.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.Term;

public class TermRowRenderer extends JPanel implements ListCellRenderer<Term> {

	private JLabel finnishFlag = new JLabel("🇫🇮");
	private JLabel finnish = new JLabel();
	private JLabel englishFlag = new JLabel("🇬🇧");
	private JLabel english = new JLabel();

	public TermRowRenderer() {
		setLayout(new GridLayout(2, 2));
		finnishFlag.setFont(Settings.emojiFont);
		englishFlag.setFont(Settings.emojiFont);
		add(finnishFlag);
		add(finnish);
		add(englishFlag);
		add(english);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Term> list, Term term, int index, boolean isSelected,
			boolean cellHasFocus) {
		finnish.setText(term.getFinnish());
		english.setText(term.getEnglish());

		if (isSelected) {
			finnish.setBackground(list.getSelectionBackground());
			english.setBackground(list.getSelectionBackground());
			finnishFlag.setBackground(list.getSelectionBackground());
			englishFlag.setBackground(list.getBackground());
			setBackground(list.getSelectionBackground());
		} else { // when don't select
			finnish.setBackground(list.getBackground());
			english.setBackground(list.getBackground());
			finnishFlag.setBackground(list.getBackground());
			englishFlag.setBackground(list.getBackground());
			setBackground(list.getBackground());
		}
		return this;
	}

}
