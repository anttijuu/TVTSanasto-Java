package fi.oulu.tol.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import fi.oulu.tol.model.Term;

public class TermRowRenderer extends JPanel implements ListCellRenderer<Term> {

	private JLabel finnish = new JLabel();
	private JLabel english = new JLabel();

	public TermRowRenderer() {
		setLayout(new GridLayout(2, 1));
		add(finnish);
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
			setBackground(list.getSelectionBackground());
		} else {
			Color color = list.getBackground();
			if (index % 2 == 1) {
				color = Color.LIGHT_GRAY;
			}
			finnish.setBackground(color);
			english.setBackground(color);
			setBackground(color);
		}
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		return this;
	}

}
