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
            updateIndex();
        } else {
            updateMap(categories);
        }
    }

    public void updateIndex() throws SQLException, IOException {
        List<TermCategory> categories = network.getIndex();
        database.saveCategories(categories);
        updateMap(categories);
    }

    public List<TermCategory> getCategories() {
        return terms.keySet().stream().toList();
    }

    private void updateMap(List<TermCategory> fromCategories) {
        for (TermCategory category : fromCategories) {
            if (!terms.containsKey(category)) {
                terms.put(category, new ArrayList<>());
            }
        }
    }
}
