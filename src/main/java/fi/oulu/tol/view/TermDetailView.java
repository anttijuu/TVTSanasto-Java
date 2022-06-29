package fi.oulu.tol.view;

import java.awt.Dimension;
import java.awt.Color;

import javax.swing.JPanel;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.Term;

public class TermDetailView extends JPanel {

	private Term term;

	public TermDetailView(Term term) {
		this.term = term;
		setPreferredSize(new Dimension(Settings.WINDOW_WIDTH - (Settings.LIST_WIDTH * 2), Settings.WINDOW_HEIGHT));
		setBackground(Color.YELLOW);
	}
}
