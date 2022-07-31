# TVT Sanasto Swing

This appication is meant to help in learning terms in the area of Information and Communication Technologies (ICT) in Finnish and English.

The terms are arranged in categories. For example, terms in the category of basic computers list terms such as bit, byte, CPU, etc. For each term, there are link(s) to futher information about the term.

The app fetches an index of term categories from a server. This file is in JSON format. The list of categories contain a link to the terms for each category, again in JSON format. These files area hosted in GitLab.

App fetches the categories and terms and stores these in the user's computer in a SQLite database. The terms can then be studied without network connection or using the bandwidth. User can later fetch updates both to the index and the categories of terms.

User may also sort the terms by language and search using keywords, app then listing only those terms containing the keyword.

## Dependencies

The app uses the following Java features and components:

* AWT and Swing for the user interface,
* org.json for parsing JSON content,
* org.xerial JDBC driver for SQLite,
* Apache log4j for logging (latest version with fixes to the recent vulnerabilities found),
* com.github.rjeschke txtmark for converting markdown text formatting to HTML in Swing JEditorPane.

Project is managed using Maven, so these dependencies are all configured in the `pom.xml`.

## License

See the `LICENSE` file included.
MIT License, (c) Antti Juustila, 2022.