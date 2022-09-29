package fi.oulu.tol.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TermGraphGenerator {

	public class GraphGeneratorException extends Exception {
		GraphGeneratorException(String message) {
			super(message);
		}
	}

	private TermProvider provider;
	private BufferedWriter writer;
	private String oldSearchFilter;

	private static final Logger logger = LogManager.getLogger(TermGraphGenerator.class);

	public TermGraphGenerator(TermProvider provider) {
		this.provider = provider;
	}

	public void buildGraph() throws IOException, SQLException, GraphGeneratorException {
		logger.info("Starting to build a GraphViz graph");
		writeHeader();
		writeTerms();
		writeFooter();
		generateImage();
		openImage();
		logger.info("Graph image generated");
	}

	private void writeHeader() throws IOException {
		logger.debug("Writing the header");
		oldSearchFilter = provider.getSearchFilter();
		writer = new BufferedWriter(new FileWriter("graph.dot", StandardCharsets.UTF_8));
		writer.write("digraph \"" + provider.getSelectedCategory().toString() + "\" {\n");
		writer.write("\tgraph [overlap=false outputorder=edgesfirst]\n");
		writer.write("\tnode [shape=ellipse style=filled fillcolor=white]\n");
	}

	private void writeTerms() throws IOException, SQLException {
		logger.debug("Writing the terms to .dot file");
		List<Term> allTerms = provider.getSelectedCategoryTerms();
		for (final Term term : allTerms) {
			String id = cleanIdForGraphViz(term.getId().toLowerCase());
			String termString = term.getFinnish().toLowerCase();
			if (termString.length() > 0) {
				writer.write("\t" + 
								  id + 
								  " [label = \"" + 
								  term.getFinnish().toLowerCase() +
								  "\n" + term.getEnglish().toLowerCase() + 
								  "\"]\n");
				List<Term> referrers = allTerms.stream()
						.filter(candidateTerm -> candidateTerm.description().toLowerCase().contains(termString)
								&& candidateTerm != term)
						.toList();
				for (Term referrer : referrers) {
					String refId = cleanIdForGraphViz(referrer.getId().toLowerCase());
					writer.write("\t" + refId + " -> " + id + "\n");
				}
			}
		}
	}

	private void writeFooter() throws IOException {
		logger.debug("Writing the end of the .dot file");
		writer.write("}");
		writer.close();
		provider.setSearchFilter(oldSearchFilter);
	}

	private void generateImage() throws GraphGeneratorException {
		logger.debug("Generating the image file using graphviz dot");
		// TODO: Check if need to add "cmd" as the first command.
		String command[] = { "dot", "graph.dot", "-Tpng", "-ograph.png" };
		try {
			logger.debug("Executing command " + Arrays.toString(command));
			Process process = Runtime.getRuntime().exec(command);

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				logger.debug(" dot output: " + line);
			}
			reader.close();

			int exitValue = process.waitFor();
			if (exitValue != 0) {
				logger.error("Abnormal process termination: " + exitValue);
			}
		} catch (IOException e) {
			logger.error("Could not generate the image: " + e.getMessage());
			throw new GraphGeneratorException("GraphViz dot kuvan generointi ei onnistunut.");
		} catch (InterruptedException e) {
			logger.error("Error in process management in image generation: " + e.getMessage());
			throw new GraphGeneratorException("GraphViz dot kuvan generointiprosessin lopetus ei onnistunut.");
		}
	}

	private enum OS {
		WINDOWS,
		UNIX
	}

	private OS getOS() {
		String os = System.getProperty("os.name");
		if (os.contains("Windows")) {
			return OS.WINDOWS;
		} else {
			return OS.UNIX;
		}
	}
	private void openImage() throws GraphGeneratorException {
		// TODO: Check if need to add "cmd" as the first command.
		logger.debug("Opening the generated image.");
		String [] command;
		if (getOS() == OS.WINDOWS) {
			command = new String [] { "cmd", "/c", "graph.png" };
		} else {
			command = new String [] { "open", "graph.png" };
		}
		try {
			logger.debug("Executing command " + Arrays.toString(command));
			Process process = Runtime.getRuntime().exec(command);

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				logger.debug(" open image output: " + line);
			}
			reader.close();

			int exitValue = process.waitFor();
			if (exitValue != 0) {
				logger.error("Abnormal process termination: " + exitValue);
			}
		} catch (IOException e) {
			logger.error("Could not open the image: " + e.getMessage());
			throw new GraphGeneratorException("Kuvaa ei saatu avattua.");
		} catch (InterruptedException e) {
			logger.error("Error in process management in opening image: " + e.getMessage());
			throw new GraphGeneratorException("Kuvaa ei saatu avattua.");
		}

	}

	

	private String cleanIdForGraphViz(final String id) {
		final String allowed = "abcdefghijklmnopqrstuvwxyz_";
		char[] chars = allowed.toCharArray();
		StringBuilder builder = new StringBuilder();
		for (char c : id.toCharArray()) {
			if (Arrays.binarySearch(chars, c) >= 0) {
				builder.append(c);
			} else {
				builder.append('_');
			}
		}
		final String words[] = { "strict", "graph", "digraph", "node", "edge", "subgraph" };
		final List<String> reservedWords = Arrays.asList(words);
		String result = builder.toString();
		if (reservedWords.contains(result)) {
			result += result;
		}
		return result;
	}
}
