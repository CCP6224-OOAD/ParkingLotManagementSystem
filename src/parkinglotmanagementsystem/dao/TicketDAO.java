package parkinglotmanagementsystem.dao;

import parkinglotmanagementsystem.model.FineScheme;
import parkinglotmanagementsystem.model.Ticket;
import parkinglotmanagementsystem.util.TimeUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

  private Connection connection;

  public TicketDAO() {
    this.connection = DatabaseManager.getInstance().getConnection();
  }

  public boolean insertTicket(Ticket ticket) {
    String sql = """
            INSERT INTO tickets
            (ticket_id, plate_number, spot_id, entry_time, exit_time, fine_scheme)
            VALUES (?, ?, ?, ?, ?, ?);
        """;

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, ticket.getTicketId());
      pstmt.setString(2, ticket.getPlateNumber());
      pstmt.setString(3, ticket.getSpotId());
      pstmt.setString(4, TimeUtil.formatForDatabase(ticket.getEntryTime()));
      pstmt.setString(5, TimeUtil.formatForDatabase(ticket.getExitTime()));
      pstmt.setString(6, ticket.getFineScheme().name());

      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      System.err.println("Failed to insert ticket: " + ticket.getTicketId());
      e.printStackTrace();
      return false;
    }
  }

  public boolean updateExitTime(String ticketId, LocalDateTime exitTime) {
    String sql = "UPDATE tickets SET exit_time = ? WHERE ticket_id = ?;";

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, TimeUtil.formatForDatabase(exitTime));
      pstmt.setString(2, ticketId);

      int rowsAffected = pstmt.executeUpdate();
      return rowsAffected > 0;
    } catch (SQLException e) {
      System.err.println("Failed to update exit time for ticket: " + ticketId);
      e.printStackTrace();
      return false;
    }
  }

  public Ticket findActiveTicket(String plateNumber) {
    String sql = """
            SELECT * FROM tickets
            WHERE plate_number = ? AND exit_time IS NULL
            ORDER BY entry_time DESC
            LIMIT 1;
        """;

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, plateNumber);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return extractTicketFromResultSet(rs);
      }
    } catch (SQLException e) {
      System.err.println("Failed to find active ticket for plate: " + plateNumber);
      e.printStackTrace();
    }

    return null;
  }

  public Ticket findTicketById(String ticketId) {
    String sql = "SELECT * FROM tickets WHERE ticket_id = ?;";

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, ticketId);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return extractTicketFromResultSet(rs);
      }
    } catch (SQLException e) {
      System.err.println("Failed to find ticket: " + ticketId);
      e.printStackTrace();
    }

    return null;
  }

  public List<Ticket> getAllActiveTickets() {
    String sql = """
            SELECT * FROM tickets
            WHERE exit_time IS NULL
            ORDER BY entry_time DESC;
        """;

    List<Ticket> tickets = new ArrayList<>();

    try (Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        tickets.add(extractTicketFromResultSet(rs));
      }
    } catch (SQLException e) {
      System.err.println("Failed to get active tickets");
      e.printStackTrace();
    }

    return tickets;
  }

  public List<Ticket> getTicketsByPlate(String plateNumber) {
    String sql = """
            SELECT * FROM tickets
            WHERE plate_number = ?
            ORDER BY entry_time DESC;
        """;

    List<Ticket> tickets = new ArrayList<>();

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, plateNumber);
      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        tickets.add(extractTicketFromResultSet(rs));
      }
    } catch (SQLException e) {
      System.err.println("Failed to get tickets for plate: " + plateNumber);
      e.printStackTrace();
    }

    return tickets;
  }

  public int getActiveTicketCount() {
    String sql = "SELECT COUNT(*) FROM tickets WHERE exit_time IS NULL;";

    try (Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      if (rs.next()) {
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      System.err.println("Failed to get active ticket count");
      e.printStackTrace();
    }

    return 0;
  }

  public int getTotalTicketCount() {
    String sql = "SELECT COUNT(*) FROM tickets;";

    try (Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      if (rs.next()) {
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      System.err.println("Failed to get total ticket count");
      e.printStackTrace();
    }

    return 0;
  }

  public boolean isVehicleParked(String plateNumber) {
    return findActiveTicket(plateNumber) != null;
  }

  private Ticket extractTicketFromResultSet(ResultSet rs) throws SQLException {
    String ticketId = rs.getString("ticket_id");
    String plateNumber = rs.getString("plate_number");
    String spotId = rs.getString("spot_id");
    LocalDateTime entryTime = TimeUtil.parseFromDatabase(rs.getString("entry_time"));
    LocalDateTime exitTime = TimeUtil.parseFromDatabase(rs.getString("exit_time"));
    FineScheme fineScheme = FineScheme.valueOf(rs.getString("fine_scheme"));

    return new Ticket(ticketId, plateNumber, spotId, entryTime, exitTime, fineScheme);
  }
}
