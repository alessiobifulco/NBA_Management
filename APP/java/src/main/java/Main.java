import core.Controller;
import core.Model;
import core.View;
import data.DAOUtils;
import view.LoginPage;

public class Main {
    public static void main(String[] args) {
        // Collega al database NBA_Management
        var connection = DAOUtils.localMySQLConnection("NBA_Management", "root", "BLS007ab&");
        System.out.println("Connessione al database avvenuta con successo.");

        var model = Model.fromConnection(connection);
        var view = new View(() -> {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        var controller = new Controller(model, view);
        view.setController(controller);

        // Avvia la schermata di login con il model
        new LoginPage(view.getFrame(), model);
    }
}
