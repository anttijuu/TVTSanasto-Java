package fi.oulu.tol;

import java.awt.Dimension;
import java.awt.MenuBar;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.ChoiceFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.ResourceBundle;

import fi.oulu.tol.model.TermGraphGenerator;
import fi.oulu.tol.model.Language;
import fi.oulu.tol.model.Term;
import fi.oulu.tol.model.TermProvider;
import fi.oulu.tol.model.TermGraphGenerator.GraphGeneratorException;
import fi.oulu.tol.view.TermCategoryListView;
import fi.oulu.tol.view.TermDetailView;
import fi.oulu.tol.view.TermListView;
import fi.oulu.tol.view.SearchPanel;

public class TVTSanasto implements ActionListener, WindowListener {

	private JFrame frame;
	private TermProvider provider;
	private static final Logger logger = LogManager.getLogger(TVTSanasto.class);
	private ResourceBundle messages;

	private static final String ABOUT_TEXT = "Tietotekniikan termejä oppijoille 1.1\n" +
														"Lisätietoja sovelluksesta ja sanastoista: " + 
														"https://github.com/anttijuu/TVTSanasto-Java" + 
														"\n\nAvoimen lähdekoodin lisenssit:\n" + 
														"com.github.rjeschke txtmark Copyright (C) 2011-2015 René Jeschke Apache License Version 2.0\n" + 
														"org.xerial JDBC SQLite driver Copyright (C) Taro L. Saito Apache License Version 2.0\n" +
														"Ulkoinen binäärityökalu (lähdekoodia ei käytetä): GraphViz: Common Public License Version 1.0";

	public static void main(String[] args) {
		logger.info("Launching TVTSanasto");
		try {
			if (args.length > 0 && (args[0].equalsIgnoreCase("-v") || args[0].equalsIgnoreCase("--version"))) {
				System.out.println(ABOUT_TEXT);
				return;
			}
			new TVTSanasto().run();
		} catch (SQLException e) {
			logger.error("SQLException in app, exiting");
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IOException in app, exiting");
			e.printStackTrace();
		} catch (URISyntaxException e) {
			logger.error("URISyntaxException in app, exiting");
			e.printStackTrace();
		}
	}

	private void run() throws SQLException, IOException, URISyntaxException {
		logger.debug("Reading settings");
		Settings.readSettings();

		messages = ResourceBundle.getBundle("TVTSanastoBundle", Settings.currentLocale());

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
		SearchPanel searchPanel = new SearchPanel(provider);
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
		JMenu appMenu = new JMenu("TVT Sanasto");
		// Maze size
		JMenuItem commandMenu = new JMenuItem("Tietoja");
		commandMenu.setActionCommand("cmd-about");
		commandMenu.addActionListener(this);
		appMenu.add(commandMenu);
		appMenu.addSeparator();

		Icon flagFi = new ImageIcon(ClassLoader.getSystemResource("images/fi.png"));
		Icon flagEn = new ImageIcon(ClassLoader.getSystemResource("images/en.png"));

		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem radioMenu = new JRadioButtonMenuItem("Suomi", flagFi, provider.getSortOrder() == Language.FINNISH);
		radioMenu.setActionCommand("sort-fi");
		radioMenu.addActionListener(this);
		group.add(radioMenu);
		appMenu.add(radioMenu);
		radioMenu = new JRadioButtonMenuItem("Englanti", flagEn, provider.getSortOrder() == Language.ENGLISH);
		radioMenu.addActionListener(this);
		radioMenu.setActionCommand("sort-en");
		group.add(radioMenu);
		appMenu.add(radioMenu);

		appMenu.addSeparator();

		commandMenu = new JMenuItem("Päivitä kategoriat");
		commandMenu.setActionCommand("cmd-refresh-index");
		commandMenu.addActionListener(this);
		appMenu.add(commandMenu);
		commandMenu = new JMenuItem("Päivitä valittu kategoria");
		commandMenu.setActionCommand("cmd-refresh-category");
		commandMenu.addActionListener(this);
		appMenu.add(commandMenu);
		appMenu.addSeparator();
		commandMenu = new JMenuItem("Luo termiverkko");
		commandMenu.setActionCommand("cmd-create-graph");
		commandMenu.addActionListener(this);
		appMenu.add(commandMenu);
		appMenu.addSeparator();
		commandMenu = new JMenuItem("Lopeta");
		commandMenu.setActionCommand("cmd-quit");
		commandMenu.addActionListener(this);
		appMenu.add(commandMenu);
		mainMenu.add(appMenu);
		frame.setJMenuBar(mainMenu);
		logger.debug("Showing app");
		frame.addWindowListener(this);
		frame.pack();
		updateUILanguage();
		frame.setVisible(true);
	}

	private void updateUILanguage() {
		messages = ResourceBundle.getBundle("TVTSanastoBundle", Settings.currentLocale());
		frame.setTitle(messages.getString("app_name_full"));
		JMenuBar mainMenu = frame.getJMenuBar();
		JMenu appMenu = mainMenu.getMenu(0);
		appMenu.setText(messages.getString("app_name_full"));
		for (int index = 0; index < appMenu.getMenuComponentCount(); index++) {
			JMenuItem menuItem = appMenu.getItem(index);
			if (menuItem == null) { continue; }
			if (menuItem.getActionCommand().equals("cmd-about")) {
				menuItem.setText(messages.getString("app_info_text"));
			} else if (menuItem.getActionCommand().equals("cmd-refresh-index")) {
				menuItem.setText(messages.getString("app_command_update_categories"));
			} else if (menuItem.getActionCommand().equals("cmd-create-graph")) {
				menuItem.setText(messages.getString("app_command_create_term_graph"));
			} else if (menuItem.getActionCommand().equals("cmd-quit")) {
				menuItem.setText(messages.getString("app_command_quit"));
			} else if (menuItem.getActionCommand().equals("sort-fi")) {
				menuItem.setText(messages.getString("lang_fi"));
			} else if (menuItem.getActionCommand().equals("sort-en")) {
				menuItem.setText(messages.getString("lang_en"));
			} else if (menuItem.getActionCommand().equals("cmd-refresh-category")) {
				menuItem.setText(messages.getString("app_command_update_selected_category"));
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("cmd-about")) {
			JOptionPane.showMessageDialog(frame, ABOUT_TEXT, messages.getString("app_name_full"), JOptionPane.INFORMATION_MESSAGE);
		} else if (e.getActionCommand().equals("sort-fi")) {
			Settings.language = Language.FINNISH;
			Settings.saveSettings();
			provider.setSortOrder(Language.FINNISH);
			updateUILanguage();
		} else if (e.getActionCommand().equals("sort-en")) {
			Settings.language = Language.ENGLISH;
			Settings.saveSettings();
			provider.setSortOrder(Language.ENGLISH);
			updateUILanguage();
		} else if (e.getActionCommand().equals("cmd-refresh-index")) {
			try {
				int newCategories = provider.fetchIndex();
				final String infoString = this.getFetchCategoryInfoMessage(newCategories, messages.getString("category"));
				JOptionPane.showMessageDialog(frame, infoString, messages.getString("app_name_full"), JOptionPane.INFORMATION_MESSAGE);
			} catch (UnknownHostException e1) {
				String message = String.format("Palvelinta ei löydy: %s\n", e1.getLocalizedMessage());
				JOptionPane.showMessageDialog(frame, message, "TVT Sanasto", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			} catch (SQLException e1) {
				String message = String.format("Tietokantavirhe: %s\n", e1.getLocalizedMessage());
				JOptionPane.showMessageDialog(frame, message, "TVT Sanasto", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			} catch (IOException e1) {
				String message = String.format("Virhe luettaessa kategorioita: %s\n", e1.getLocalizedMessage());
				JOptionPane.showMessageDialog(frame, message, "TVT Sanasto", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			} catch (URISyntaxException e1) {
				String message = String.format("Virhe sanaston osoitteessa: %s%n", e1.getLocalizedMessage());
				JOptionPane.showMessageDialog(frame, message, "TVT Sanasto", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		} else if (e.getActionCommand().equals("cmd-refresh-category")) {
			try {
				int oldTermCount = provider.getSelectedCategoryTerms().size();
				List<Term> terms = provider.fetchTerms(provider.getSelectedCategory());
				final String infoString = this.getFetchCategoryInfoMessage(terms.size()-oldTermCount, messages.getString("term"));
				JOptionPane.showMessageDialog(frame, infoString, messages.getString("app_name_full"), JOptionPane.INFORMATION_MESSAGE);
			} catch (SQLException | IOException e1) {
				String message = String.format("Virhe haettaessa termejä: %s\n", e1.getLocalizedMessage());
				JOptionPane.showMessageDialog(frame, message, "TVT Sanasto", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			} catch (JSONException e1) {
				String message = String.format("Virhe sanastossa: %s\n", e1.getLocalizedMessage());
				JOptionPane.showMessageDialog(frame, message, "TVT Sanasto", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			} catch (URISyntaxException e1) {
				String message = String.format("Virhe sanaston osoitteessa: %s\n", e1.getLocalizedMessage());
				JOptionPane.showMessageDialog(frame, message, "TVT Sanasto", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		} else if (e.getActionCommand().equals("cmd-create-graph")) {
			String error = null;
			String graphVizError = "";
			try {
				new TermGraphGenerator(provider).buildGraph();
			} catch (SQLException | IOException e1) {
				e1.printStackTrace();
				error = e1.getMessage();
			} catch (GraphGeneratorException e1) {
				e1.printStackTrace();
				error = e1.getMessage();
				graphVizError = "Onko GraphViz asennettu ja käytettävissä komentoriviltä?";
			} catch (JSONException e1) {
				e1.printStackTrace();
				error = e1.getMessage();
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
				error = e1.getMessage();
			}
			if (null != error) {
				String message = String.format("Termiverkkoa ei saatu luotua.\n%s\nVirhe: %s", graphVizError, error);
				JOptionPane.showMessageDialog(frame, message, "TVT Sanasto", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getActionCommand().equals("cmd-quit")) {
			close();
		} else {
			logger.error("Unknown menu command selected");
		}
	}

	private String getFetchCategoryInfoMessage(int countOfNewCategories, final String termOrCategory) {
		MessageFormat messageForm = new MessageFormat("fetch_pattern");
		messageForm.setLocale(Settings.currentLocale());
		double[] termCountLimits = {0,1,2};
		String [] termStrings = {
			messages.getString("no_new_items"),
			messages.getString("one_new_item"),
			messages.getString("many_new_items")
		};
		ChoiceFormat choiceForm = new ChoiceFormat(termCountLimits, termStrings);
		String pattern = messages.getString("fetch_pattern");
		messageForm.applyPattern(pattern);
		Format[] formats = {choiceForm, null, NumberFormat.getInstance()};
		messageForm.setFormats(formats);
		Object[] messageArguments = {countOfNewCategories, termOrCategory};
		final String result = messageForm.format(messageArguments);
		return result;
	}

	private void close() {
		logger.info("Quitting the app");
		provider.close();
		frame.dispose();
		logger.info("Main frame disposed, process should end soon.");
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// Empty
	}

	@Override
	public void windowClosing(WindowEvent e) {
		close();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// Empty
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// Empty
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// Empty
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// Empty
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// Empty
	}

}