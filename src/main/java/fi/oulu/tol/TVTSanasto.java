package fi.oulu.tol;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.FontFormatException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import fi.oulu.tol.model.Language;
import fi.oulu.tol.model.TermProvider;
import fi.oulu.tol.view.TermCategoryListView;
import fi.oulu.tol.view.TermDetailView;
import fi.oulu.tol.view.TermListView;

// TODO: Add language symbols (flags)
// TODO: Add error messages
// TODO: Add functionality to prevent too frequent fetches from server.

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
		logger.debug("Reading settings");
		Settings.readSettings();
		logger.info("Index last fetched: " + Settings.lastIndexFetchDateTime.toString());
		logger.info("Selected language/sortorder: " + Settings.language);
		logger.debug("Creating TermProvider");
		provider = new TermProvider();
		logger.debug("Initializing Swing GUI");
		frame = new JFrame("TVT Sanasto");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setPreferredSize(new Dimension(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT));

		JSplitPane rootPanel = new JSplitPane();
		rootPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
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
		searchPanel.add(searchField);
		searchPanel.add(clearButton);
		rootPanel.setTopComponent(searchPanel);

		JSplitPane categoryPanel = new JSplitPane();
		frame.getContentPane().add(rootPanel);
		categoryPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		categoryPanel.setTopComponent(new TermCategoryListView(provider));
		JSplitPane detailPanel = new JSplitPane();
		detailPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		detailPanel.setTopComponent(new TermListView(provider));
		detailPanel.setBottomComponent(new TermDetailView(provider));
		categoryPanel.setBottomComponent(detailPanel);
		rootPanel.setBottomComponent(categoryPanel);

		logger.debug("Initializing Menus");
		JMenuBar mainMenu = new JMenuBar();
		JMenu mazeMenu = new JMenu("TVT Sanasto");
		// Maze size
		JMenuItem commandMenu = new JMenuItem("Tietoja");
		commandMenu.setActionCommand("cmd-about");
		commandMenu.addActionListener(this);
		mazeMenu.add(commandMenu);
		mazeMenu.addSeparator();

		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem radioMenu = new JRadioButtonMenuItem("Suomi", provider.getSortOrder() == Language.FINNISH);
		radioMenu.setActionCommand("sort-fi");
		radioMenu.addActionListener(this);
		group.add(radioMenu);
		mazeMenu.add(radioMenu);
		radioMenu = new JRadioButtonMenuItem("Englanti", provider.getSortOrder() == Language.ENGLISH);
		radioMenu.addActionListener(this);
		radioMenu.setActionCommand("sort-en");
		group.add(radioMenu);
		mazeMenu.add(radioMenu);

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
		commandMenu = new JMenuItem("Lopeta");
		commandMenu.setActionCommand("cmd-quit");
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
			// TODO: Implement
		} else if (e.getActionCommand().equals("sort-fi")) {
			provider.setSortOrder(Language.FINNISH);
			Settings.language = Language.FINNISH;
			Settings.saveSettings();
		} else if (e.getActionCommand().equals("sort-en")) {
			provider.setSortOrder(Language.ENGLISH);
			Settings.language = Language.ENGLISH;
			Settings.saveSettings();
		} else if (e.getActionCommand().equals("cmd-refresh-index")) {
			try {
				provider.fetchIndex();
				Settings.lastIndexFetchDateTime = LocalDateTime.now(ZoneOffset.UTC);
				Settings.saveSettings();
			} catch (SQLException | IOException e1) {
				e1.printStackTrace();
			}
		} else if (e.getActionCommand().equals("cmd-refresh-category")) {
			try {
				provider.fetchTerms(provider.getSelectedCategory());
			} catch (SQLException | IOException e1) {
				e1.printStackTrace();
			}
		} else if (e.getActionCommand().equals("cmd-quit")) {
			logger.info("Quitting the app");
			provider.close();
			frame.dispose();
		} else {
			logger.error("Unknown menu command selected");
		}
	}

}