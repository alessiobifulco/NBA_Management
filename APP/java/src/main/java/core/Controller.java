package core;

import java.sql.SQLException;
import java.util.List;

public class Controller {

    private final Model model;
    private final View view;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
    }
}
