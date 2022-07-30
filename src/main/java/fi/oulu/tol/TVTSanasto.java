package fi.oulu.tol;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.FontFormatException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;

import fi.oulu.tol.model.TermProvider;
import fi.oulu.tol.view.TermCategoryListView;
import fi.oulu.tol.view.TermDetailView;
import fi.oulu.tol.view.TermListView;

public class TVTSanasto implements ActionListener {

	private JFrame frame;
	private TermProvider provider;
	private static final Logger logger = LogManager.getLogger(TVTSanasto.class);
	
	public static void main(String[] args) {
		logger.info("Launching TVTSanasto");
		try {
			new TVTSanasto().run();
		} catch (SQLException e) {
			logger.error("SQLException in app, exiting");
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IOException in app, exiting");
			e.printStackTrace();
		} catch (FontFormatException e) {
			logger.error("FontFormatException in app, exiting");
			e.printStackTrace();
		}
	}

	private void run() throws SQLException, IOException, FontFormatException {
		Settings.installEmojiFont();
		logger.debug("Initializing TermProvider");
		provider = new TermProvider();
		logger.debug("Initializing Swing GUI");
		frame = new JFrame("TVT Sanasto");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT));

		JSplitPane rootPanel = new JSplitPane();
		frame.getContentPane().add(rootPanel);
		rootPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		rootPanel.setTopComponent(new TermCategoryListView(provider));
		JSplitPane detailPanel = new JSplitPane();
		detailPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		detailPanel.setTopComponent(new TermListView(provider));
		detailPanel.setBottomComponent(new TermDetailView(provider));
		rootPanel.setBottomComponent(detailPanel);

		logger.debug("Initializing Menus");
		JMenuBar mainMenu = new JMenuBar();
		JMenu mazeMenu = new JMenu("TVT Sanasto");
		// Maze size
		JMenuItem commandMenu = new JMenuItem("Tietoja");
		commandMenu.setActionCommand("cmd-about");
		commandMenu.addActionListener(this);
		mazeMenu.add(commandMenu);
		mazeMenu.addSeparator();
		commandMenu = new JMenuItem("Päivitä kategoriat");
		commandMenu.setActionCommand("cmd-refresh-index");
		commandMenu.addActionListener(this);
		mazeMenu.add(commandMenu);
		commandMenu = new JMenuItem("Päivitä valittu kategoria");
		commandMenu.setActionCommand("cmd-refresh-category");
		commandMenu.addActionListener(this);
		mazeMenu.add(commandMenu);
		mazeMenu.addSeparator();
		commandMenu = new JMenuItem("Asetukset");
		commandMenu.setActionCommand("cmd-settings");
		commandMenu.addActionListener(this);
		mazeMenu.add(commandMenu);		
		mainMenu.add(mazeMenu);
		frame.setJMenuBar(mainMenu);
		logger.debug("Showing app");
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("cmd-about")) {

		} else if (e.getActionCommand().equals("cmd-refresh-index")) {
			try {
				provider.fetchIndex();
			} catch (SQLException | IOException e1) {
				e1.printStackTrace();
			}
		} else if (e.getActionCommand().equals("cmd-refresh-category")) {
			try {
				provider.fetchTerms(provider.getSelectedCategory());
			} catch (SQLException | IOException e1) {
				e1.printStackTrace();
			}
		} else if (e.getActionCommand().equals("cmd-settings")) {

		} else {
			logger.error("Unknown menu command selected");
		}
		
	}
}