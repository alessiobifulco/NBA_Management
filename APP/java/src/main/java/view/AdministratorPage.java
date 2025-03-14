package view;

import core.Model;
import javax.swing.*;
import java.awt.*;

import java.sql.SQLException;

public class AdministratorPage extends JFrame {

    private Model model;

    public AdministratorPage(JFrame jFrame, Model model) {
        this.model = model;

        setTitle("Administrator");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Back button to return to the Login page
        JButton backButton = new JButton("Go Back to Login");
        backButton.addActionListener(e -> {
            new LoginPage(jFrame, model); // Return to the login screen
            dispose(); // Close the current window
        });

        // Add or remove players
        JButton addRemovePlayerButton = new JButton("Add/Remove Player");
        addRemovePlayerButton.addActionListener(e -> {
            String playerName = JOptionPane.showInputDialog(this, "Enter player name:");
            if (playerName != null) {
                String action = JOptionPane.showInputDialog(this, "Enter action (add/remove):");
                if ("add".equalsIgnoreCase(action)) {
                    String surname = JOptionPane.showInputDialog(this, "Enter surname:");
                    String position = JOptionPane.showInputDialog(this, "Enter position:");
                    String category = JOptionPane.showInputDialog(this, "Enter category:");
                    double rating = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter rating:"));
                    int experience = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter years of experience:"));

                    try {
                        model.addPlayer(playerName, surname, position, category, rating, experience);
                        JOptionPane.showMessageDialog(this, "Player added successfully!");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error adding player: " + ex.getMessage());
                    }
                } else if ("remove".equalsIgnoreCase(action)) {
                    int playerId = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter player ID to remove:"));
                    try {
                        model.removePlayer(playerId);
                        JOptionPane.showMessageDialog(this, "Player removed successfully!");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error removing player: " + ex.getMessage());
                    }
                }
            }
        });

        // Add or remove coaches
        JButton addRemoveCoachButton = new JButton("Add/Remove Coach");
        addRemoveCoachButton.addActionListener(e -> {
            String coachName = JOptionPane.showInputDialog(this, "Enter coach name:");
            if (coachName != null) {
                String action = JOptionPane.showInputDialog(this, "Enter action (add/remove):");
                if ("add".equalsIgnoreCase(action)) {
                    String surname = JOptionPane.showInputDialog(this, "Enter surname:");
                    double salary = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter salary:"));
                    int experience = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter years of experience:"));

                    try {
                        model.addCoach(coachName, surname, salary, experience);
                        JOptionPane.showMessageDialog(this, "Coach added successfully!");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error adding coach: " + ex.getMessage());
                    }
                } else if ("remove".equalsIgnoreCase(action)) {
                    int coachId = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter coach ID to remove:"));
                    try {
                        model.removeCoach(coachId);
                        JOptionPane.showMessageDialog(this, "Coach removed successfully!");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error removing coach: " + ex.getMessage());
                    }
                }
            }
        });

        // Add or remove observers
        JButton addRemoveObserverButton = new JButton("Add/Remove Observer");
        addRemoveObserverButton.addActionListener(e -> {
            String observerName = JOptionPane.showInputDialog(this, "Enter observer name:");
            if (observerName != null) {
                String action = JOptionPane.showInputDialog(this, "Enter action (add/remove):");
                if ("add".equalsIgnoreCase(action)) {
                    String surname = JOptionPane.showInputDialog(this, "Enter surname:");
                    double salary = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter salary:"));
                    int experience = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter years of experience:"));

                    try {
                        model.addObserver(observerName, surname, salary, experience);
                        JOptionPane.showMessageDialog(this, "Observer added successfully!");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error adding observer: " + ex.getMessage());
                    }
                } else if ("remove".equalsIgnoreCase(action)) {
                    int observerId = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter observer ID to remove:"));
                    try {
                        model.removeObserver(observerId);
                        JOptionPane.showMessageDialog(this, "Observer removed successfully!");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error removing observer: " + ex.getMessage());
                    }
                }
            }
        });

        // Add or remove games
        JButton addRemoveGameButton = new JButton("Add/Remove Game");
        addRemoveGameButton.addActionListener(e -> {
            String action = JOptionPane.showInputDialog(this, "Enter action (add/remove):");
            if ("add".equalsIgnoreCase(action)) {
                int team1Id = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter team 1 ID:"));
                int team2Id = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter team 2 ID:"));
                String result = JOptionPane.showInputDialog(this, "Enter result:");

                try {
                    model.addGame(team1Id, team2Id, result);
                    JOptionPane.showMessageDialog(this, "Game added successfully!");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error adding game: " + ex.getMessage());
                }
            } else if ("remove".equalsIgnoreCase(action)) {
                int gameId = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter game ID to remove:"));
                try {
                    model.removeGame(gameId);
                    JOptionPane.showMessageDialog(this, "Game removed successfully!");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error removing game: " + ex.getMessage());
                }
            }
        });

        // Add buttons to the frame
        add(backButton);
        add(addRemovePlayerButton);
        add(addRemoveCoachButton);
        add(addRemoveObserverButton);
        add(addRemoveGameButton);

        setVisible(true);
    }
}
