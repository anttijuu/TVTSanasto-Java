package fi.oulu.tol.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import fi.oulu.tol.model.TermCategory;

public class CategoryRowRenderer extends JPanel implements ListCellRenderer<TermCategory> {

	private JLabel name= new JLabel();

	public CategoryRowRenderer() {
		setLayout(new GridLayout(1, 1));
		add(name);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends TermCategory> list, TermCategory category, int index, boolean isSelected,
			boolean cellHasFocus) {
		name.setText(category.toString());

		if (isSelected) {
			name.setBackground(list.getSelectionBackground());
			setBackground(list.getSelectionBackground());
		} else {
			Color color = list.getBackground();
			if (index % 2 == 1) {
				color = Color.LIGHT_GRAY;
			}
			name.setBackground(color);
			setBackground(color);
		}
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		return this;
	}

}
