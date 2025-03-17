package model;

import data.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import core.Model;

public class DBModel implements Model {

    private final Connection connection;

    public DBModel(Connection connection) {
        Objects.requireNonNull(connection, "Model created with null connection");
        this.connection = connection;
    }

    // Funzione per ottenere la rosa di una squadra
    @Override
    public List<Player> getTeamRoster(int idTeam) throws SQLException {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT G.idGiocatore, G.nome, G.cognome, G.position, G.categoria, G.valutazione, G.freeagent " +
                "FROM GIOCATORE G " +
                "JOIN CONTRATTO C ON G.idGiocatore = C.idGiocatore " +
                "WHERE C.idSquadra = ? AND C.stato = TRUE";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idTeam);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Player player = new Player(
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getInt("age"),
                        Player.Position.valueOf(rs.getString("position")),
                        Player.Category.valueOf(rs.getString("categoria")),
                        rs.getInt("rank"),
                        rs.getInt("expereince"),
                        rs.getBoolean("freeagent"));
                player.setIdPlayer(rs.getInt("idGiocatore"));
                players.add(player);
            }
        }
        return players;
    }

    // Funzione per cercare free agent
    @Override
    public List<Player> getFreeAgents() throws SQLException {
        List<Player> freeAgents = new ArrayList<>();
        String sql = "SELECT idGiocatore, nome, cognome, position, categoria, valutazione " +
                "FROM GIOCATORE WHERE freeagent = TRUE";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Player player = new Player(
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getInt("age"),
                        Player.Position.valueOf(rs.getString("position")),
                        Player.Category.valueOf(rs.getString("categoria")),
                        rs.getInt("rank"),
                        rs.getInt("expereince"),
                        rs.getBoolean("freeagent"));
                player.setIdPlayer(rs.getInt("idGiocatore"));
                freeAgents.add(player);
            }
        }
        return freeAgents;
    }

    // Funzione per creare una proposta di scambio
    @Override
    public void createTradeProposal(int idContract1, int idContract2) throws SQLException {
        String sql = "INSERT INTO SCAMBIO (idContratto1, idContratto2, risultato, data) " +
                "VALUES (?, ?, 'In corso', NOW())";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idContract1);
            stmt.setInt(2, idContract2);
            stmt.executeUpdate();
        }
    }

    // Funzione per aggiornare lo stato di uno scambio
    @Override
    public void updateTradeStatus(int idScambio, String status) throws SQLException {
        String sql = "UPDATE SCAMBIO SET risultato = ? WHERE idScambio = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, idScambio);
            stmt.executeUpdate();
        }
    }

    // Funzione per visualizzare lo storico delle partite
    @Override
    public List<String> getMatchHistory(int idTeam) throws SQLException {
        List<String> history = new ArrayList<>();
        String sql = "SELECT P.data, P.risultato, S1.nome AS squadra_casa, S2.nome AS squadra_ospite " +
                "FROM PARTITA P " +
                "JOIN SQUADRA S1 ON P.idSquadra1 = S1.idSquadra " +
                "JOIN SQUADRA S2 ON P.idSquadra2 = S2.idSquadra " +
                "WHERE S1.idSquadra = ? OR S2.idSquadra = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idTeam);
            stmt.setInt(2, idTeam);
            ResultSet rs = stmt.executeQuery();

            int count = 0; // Contatore per vedere quante righe vengono restituite
            while (rs.next()) {
                history.add(rs.getString("data") + " - " +
                        rs.getString("risultato") + " - " +
                        rs.getString("squadra_casa") + " vs " +
                        rs.getString("squadra_ospite"));
                count++;
            }

            System.out.println("Number of matches found: " + count); // Stampa del conteggio
        }
        return history;
    }

    // Funzione per ottenere i contratti in scadenza
    @Override
    public List<Contract> getExpiringContracts() throws SQLException {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT C.idContratto, C.idGiocatore, C.idSquadra, C.stipendio, C.data, C.durata, C.stato " +
                "FROM CONTRATTO C WHERE C.stato = TRUE AND C.durata = 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Contract contract = new Contract(
                        rs.getInt("idGiocatore"),
                        rs.getInt("idSquadra"),
                        rs.getInt("stipendio"),
                        rs.getDate("data").toLocalDate(),
                        rs.getInt("durata"),
                        rs.getBoolean("stato"));
                contract.setIdContract(rs.getInt("idContratto"));
                contracts.add(contract);
            }
        }
        return contracts;
    }

    // Funzione per registrare l'accesso di un GM
    @Override
    public boolean registerGMAccess(String email, String password) {
        String sql = "SELECT idGM FROM GM WHERE mail = ? AND password = ? AND idGM IN (SELECT idGM FROM SQUADRA)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int idGM = rs.getInt("idGM");
                System.out.println("Login valido per idGM: " + idGM);

                String insertAccess = "INSERT INTO ACCESSO (data, idGM) VALUES (NOW(), ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertAccess)) {
                    insertStmt.setInt(1, idGM);
                    insertStmt.executeUpdate();
                }

                return true; // Login e registrazione accesso riusciti
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log dell'errore
            System.out.println("Login fallito: Nessun GM trovato con queste credenziali.");

        }
        return false; // Login fallito
    }

    // Funzione per aggiungere un contratto a una squadra
    @Override
    public void addContractToTeam(int idPlayer, int idTeam, int salary, int years) throws SQLException {
        String sql = "INSERT INTO CONTRATTO (idSquadra, idGiocatore, data, durata, stipendio, stato) " +
                "VALUES (?, ?, NOW(), ?, ?, TRUE)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idTeam);
            stmt.setInt(2, idPlayer);
            stmt.setInt(3, years);
            stmt.setInt(4, salary);
            stmt.executeUpdate();
        }
    }

    // Funzione per pianificare un allenamento
    @Override
    public void scheduleTraining(int idCoach, int idPlayer, int duration, String focus) throws SQLException {
        String sql = "INSERT INTO ALLENAMENTO (idAllenatore, categoria, idGiocatore, durata, data, focus) " +
                "VALUES (?, 'Gruppo', ?, ?, NOW(), ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idCoach);
            stmt.setInt(2, idPlayer);
            stmt.setInt(3, duration);
            stmt.setString(4, focus);
            stmt.executeUpdate();
        }
    }

    // Funzione per aggiungere esercizi all'allenamento
    @Override
    public void addExerciseToTraining(int idTraining, int idExercise, int series) throws SQLException {
        String sql = "INSERT INTO ESERCIZIO_IN_ALLENAMENTO (idAllenamento, idEsercizio, serie) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idTraining);
            stmt.setInt(2, idExercise);
            stmt.setInt(3, series);
            stmt.executeUpdate();
        }
    }

    @Override
    public void addPlayer(String nome, String cognome, String position, String categoria,
            double valutazione, int anniEsperienza) throws SQLException {
        String sql = "INSERT INTO GIOCATORE (nome, cognome, position, categoria, valutazione, anni_esperienza, freeagent) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, FALSE)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, cognome);
            stmt.setString(3, position);
            stmt.setString(4, categoria);
            stmt.setDouble(5, valutazione);
            stmt.setInt(6, anniEsperienza);
            stmt.executeUpdate();
        }
    }

    @Override
    public void removePlayer(int idGiocatore) throws SQLException {
        String sql = "DELETE FROM GIOCATORE WHERE idGiocatore = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idGiocatore);
            stmt.executeUpdate();
        }
    }

    @Override
    public void addCoach(String nome, String cognome, double stipendio, int anniEsperienza) throws SQLException {
        String sql = "INSERT INTO ALLENATORE (nome, cognome, stipendio, anni_esperienza, free) " +
                "VALUES (?, ?, ?, ?, FALSE)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, cognome);
            stmt.setDouble(3, stipendio);
            stmt.setInt(4, anniEsperienza);
            stmt.executeUpdate();
        }
    }

    @Override
    public void removeCoach(int idAllenatore) throws SQLException {
        String sql = "DELETE FROM ALLENATORE WHERE idAllenatore = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idAllenatore);
            stmt.executeUpdate();
        }
    }

    @Override
    public void addObserver(String nome, String cognome, double stipendio, int anniEsperienza) throws SQLException {
        String sql = "INSERT INTO OSSERVATORE (nome, cognome, stipendio, anni_esperienza, free) " +
                "VALUES (?, ?, ?, ?, FALSE)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, cognome);
            stmt.setDouble(3, stipendio);
            stmt.setInt(4, anniEsperienza);
            stmt.executeUpdate();
        }
    }

    @Override
    public void removeObserver(int idOsservatore) throws SQLException {
        String sql = "DELETE FROM OSSERVATORE WHERE idOsservatore = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idOsservatore);
            stmt.executeUpdate();
        }
    }

    @Override
    public void addGame(int idSquadra1, int idSquadra2, String risultato) throws SQLException {
        String sql = "INSERT INTO PARTITA (idSquadra1, idSquadra2, risultato, data) " +
                "VALUES (?, ?, ?, NOW())";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idSquadra1);
            stmt.setInt(2, idSquadra2);
            stmt.setString(3, risultato);
            stmt.executeUpdate();
        }
    }

    @Override
    public void removeGame(int idPartita) throws SQLException {
        String sql = "DELETE FROM PARTITA WHERE idPartita = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPartita);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<String> getTrainingHistory(int idTeam) throws SQLException {
        List<String> trainingHistory = new ArrayList<>();

        // Query che unisce SQUADRA, ALLENATORE e ALLENAMENTO
        String sql = "SELECT A.data, A.focus, G.nome AS nome_giocatore, G.cognome AS cognome_giocatore " +
                "FROM ALLENAMENTO A " +
                "JOIN ALLENATORE AL ON A.idAllenatore = AL.idAllenatore " +
                "JOIN GIOCATORE G ON A.idGiocatore = G.idGiocatore " +
                "WHERE AL.idAllenatore = (SELECT idAllenatore FROM SQUADRA WHERE idSquadra = ?) " + // Risale
                                                                                                    // all'allenatore
                                                                                                    // della squadra
                "ORDER BY A.data DESC"; // Ordina per data

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idTeam); // Passi l'idSquadra (idTeam) come parametro per ottenere l'allenatore
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String record = "Date: " + rs.getString("data") + ", Focus: " + rs.getString("focus") +
                        ", Player: " + rs.getString("nome_giocatore") + " " + rs.getString("cognome_giocatore");
                trainingHistory.add(record);
            }
        }
        return trainingHistory;
    }

    @Override
    public List<String> getTradeHistory(int idTeam) throws SQLException {
        List<String> tradeHistory = new ArrayList<>();
        String sql = "SELECT S1.nome AS squadra_iniziale, S2.nome AS squadra_destinazione, " +
                "P1.nome AS player_iniziale, P2.nome AS player_destinazione, " +
                "T.data, T.risultato " +
                "FROM SCAMBIO T " +
                "JOIN CONTRATTO C1 ON T.idContratto1 = C1.idContratto " +
                "JOIN CONTRATTO C2 ON T.idContratto2 = C2.idContratto " +
                "JOIN SQUADRA S1 ON C1.idSquadra = S1.idSquadra " +
                "JOIN SQUADRA S2 ON C2.idSquadra = S2.idSquadra " +
                "JOIN GIOCATORE P1 ON C1.idGiocatore = P1.idGiocatore " +
                "JOIN GIOCATORE P2 ON C2.idGiocatore = P2.idGiocatore " +
                "WHERE S1.idSquadra = ? OR S2.idSquadra = ? " +
                "ORDER BY T.data DESC"; // Orders by most recent trade

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idTeam);
            stmt.setInt(2, idTeam);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String record = "Date: " + rs.getString("data") + ", Result: " + rs.getString("risultato") +
                        ", From: " + rs.getString("squadra_iniziale") + " to " + rs.getString("squadra_destinazione") +
                        ", Players: " + rs.getString("player_iniziale") + " -> " + rs.getString("player_destinazione");
                tradeHistory.add(record);
            }
        }
        return tradeHistory;
    }

    @Override
    public List<String> getContractHistory(int idTeam) throws SQLException {
        List<String> contractHistory = new ArrayList<>();
        String sql = "SELECT G.nome AS nome_giocatore, G.cognome AS cognome_giocatore, " +
                "C.data, C.durata, C.stipendio, S.nome AS squadra_nome " +
                "FROM CONTRATTO C " +
                "JOIN GIOCATORE G ON C.idGiocatore = G.idGiocatore " +
                "JOIN SQUADRA S ON C.idSquadra = S.idSquadra " +
                "WHERE S.idSquadra = ? AND C.stato = TRUE " +
                "ORDER BY C.data DESC"; // Orders by most recent contract

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idTeam);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String record = "Player: " + rs.getString("nome_giocatore") + " " + rs.getString("cognome_giocatore") +
                        ", Team: " + rs.getString("squadra_nome") + ", Date: " + rs.getString("data") +
                        ", Duration: " + rs.getInt("durata") + " years, Salary: " + rs.getDouble("stipendio");
                contractHistory.add(record);
            }
        }
        return contractHistory;
    }

    @Override
    public int getTeamIdByGM(String username, String password) {
        int teamId = -1;
        String query = "SELECT S.idSquadra FROM SQUADRA S " +
                "JOIN GM G ON S.idGM = G.idGM " +
                "WHERE G.mail = ? AND G.password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                teamId = rs.getInt("idSquadra"); // Ottieni idSquadra dalla tabella SQUADRA
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teamId;
    }

    // Nel Model (aggiungi questo metodo)
    @Override
    public void removePlayerByNameAndSurname(String playerName, String playerSurname) throws SQLException {
        String sql = "DELETE FROM Player WHERE name = ? AND surname = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerName);
            stmt.setString(2, playerSurname);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No player found with name: " + playerName + " and surname: " + playerSurname);
            }
        } catch (SQLException ex) {
            throw new SQLException("Error removing player: " + ex.getMessage());
        }
    }

    @Override
    public void removeObserverByNameAndSurname(String name, String surname) throws SQLException {
        String query = "DELETE FROM Observer WHERE name = ? AND surname = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, surname);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No observer found with the provided name and surname.");
            }
        } catch (SQLException ex) {
            throw new SQLException("Error removing observer: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void removeCoachByNameAndSurname(String name, String surname) throws SQLException {
        String query = "DELETE FROM Coach WHERE name = ? AND surname = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, surname);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No coach found with the provided name and surname.");
            }
        } catch (SQLException ex) {
            throw new SQLException("Error removing coach: " + ex.getMessage(), ex);
        }
    }
    
    

}
