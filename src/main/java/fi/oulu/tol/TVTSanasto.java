package fi.oulu.tol;

import java.io.IOException;
import java.sql.SQLException;

import fi.oulu.tol.model.TermProvider;

public class TVTSanasto {

    public static void main(String[] args) {
        try {
            TermProvider provider = new TermProvider();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}