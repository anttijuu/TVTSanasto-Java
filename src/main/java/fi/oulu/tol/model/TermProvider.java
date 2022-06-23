package fi.oulu.tol.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.oulu.tol.networking.Downloader;

public class TermProvider {
    
    private Map<TermCategory, List<Term>> terms = new HashMap<>();
    private LocalDatabase database = new LocalDatabase();
    private Downloader network = new Downloader();

    public TermProvider() throws SQLException, IOException {
        database.open("test.sqlite");
        List<TermCategory> categories = database.readCategories();
        if (categories.isEmpty()) {
            categories = network.getIndex();
            for (TermCategory category : categories) {
                if (!terms.containsKey(category)) {
                    terms.put(category, new ArrayList<>());
                }
            }
            
        }
    }
}
