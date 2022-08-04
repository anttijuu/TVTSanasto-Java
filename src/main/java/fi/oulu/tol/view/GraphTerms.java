package fi.oulu.tol.view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.Language;
import fi.oulu.tol.model.Term;
import fi.oulu.tol.model.TermProvider;

public class GraphTerms {

	private TermProvider provider;
	private BufferedWriter writer;
	private String oldSearchFilter;

	public GraphTerms(TermProvider provider) {
		this.provider = provider;
	}

	public void buildGraph() throws IOException, SQLException {
		writeHeader();
		writeTerms();
		writeFooter();
		generateImage();
		openImage();
	}

	private void writeHeader() throws IOException {
		oldSearchFilter = provider.getSearchFilter();
		writer = new BufferedWriter(new FileWriter("graph.dot", StandardCharsets.UTF_8));
		writer.write("digraph \"" + provider.getSelectedCategory().toString() + "\" {\n");
		writer.write("\tgraph [overlap=false outputorder=edgesfirst]\n");
		writer.write("\tnode [shape=ellipse style=filled fillcolor=white]\n");
	}

	private void writeTerms() throws IOException, SQLException {
		List<Term> allTerms = provider.getSelectedCategoryTerms();
		for (final Term term : allTerms) {
			String termString;
			if (Settings.language == Language.FINNISH) {
				termString = term.getFinnish().toLowerCase();
			} else {
				termString = term.getEnglish().toLowerCase();
			}
			String id = cleanIdForGraphViz(term.getId().toLowerCase());
			writer.write("\t" + id + " [label = \"" + termString + "\"]\n");
			if (termString.length() > 0) {
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
		writer.write("}");
		writer.close();
		provider.setSearchFilter(oldSearchFilter);
	}

	private void generateImage() {
		// TODO: Check if need to add "cmd" as the first command.
		String command[] = { "dot", "graph.dot", "-Tpng", "-ograph.png" };
		try {
			Process process = Runtime.getRuntime().exec(command);

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			reader.close();

			int exitValue = process.waitFor();
			if (exitValue != 0) {
				System.out.println("Abnormal process termination");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void openImage() {
		// TODO: Check if need to add "cmd" as the first command.
		String command[] = { "open", "graph.png" };
		try {
			Process process = Runtime.getRuntime().exec(command);

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			reader.close();

			int exitValue = process.waitFor();
			if (exitValue != 0) {
				System.out.println("Abnormal process termination");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private String cleanIdForGraphViz(final String id) {
		final String allowed = "abcdefghijklmnopqrstuvwxyz_";
		char[] chars = allowed.toCharArray();
		StringBuilder builder = new StringBuilder();
		for (char c : id.toCharArray()) {
			if (Arrays.binarySearch(chars, c) >= 0) {
				builder.append(c);
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
