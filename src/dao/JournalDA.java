/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author marionne pascual
 */
import javax.swing.*;
import java.sql.*;
import model.User;
import java.util.*;

public class JournalDA {
    
    public static int countJournalDetails(int journalId) {
        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = "SELECT COUNT(*) FROM journaldetails WHERE journal_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, journalId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }
    
  
    public static int createNewJournal(){
        try (Connection conn = ConnectionDB.getConnection()) {
       
            String insertJournal = "INSERT INTO journal() VALUES ()";
            PreparedStatement psJournal = conn.prepareStatement(insertJournal, Statement.RETURN_GENERATED_KEYS);
            psJournal.executeUpdate();
            
            ResultSet rs = psJournal.getGeneratedKeys();
           
            if (rs.next())
                return rs.getInt(1);
            
        }catch(SQLException e){
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }
    
    
    public static int addJournalEntry(int journalId, User user) {
        try (Connection conn = ConnectionDB.getConnection()) {
            int count = countJournalDetails(journalId);
            
            if (count >= 2) {
                 String companyQuery = "SELECT company_name FROM journal WHERE journal_id = ?";
                PreparedStatement psCompany = conn.prepareStatement(companyQuery);
                psCompany.setInt(1, journalId);
                ResultSet rsCompany = psCompany.executeQuery();
                
                String companyName = "Unknown";
                if (rsCompany.next()) {
                    companyName = rsCompany.getString("company_name");
                }


                String insertJournal = "INSERT INTO journal (company_name) VALUES (?)";
                PreparedStatement psInsert = conn.prepareStatement(insertJournal, Statement.RETURN_GENERATED_KEYS);
                psInsert.setString(1, companyName);
                psInsert.executeUpdate();

                ResultSet rsNew = psInsert.getGeneratedKeys();
                if (rsNew.next()) {
                    journalId = rsNew.getInt(1); 
                    JOptionPane.showMessageDialog(null, 
                        "Previous journal is full. Created new Journal ID: " + journalId);
                }

            }
            
            String getAccountIdQuery = "SELECT account_id FROM accounts WHERE account_title = ?";
            PreparedStatement psGetAccountId = conn.prepareStatement(getAccountIdQuery);
            psGetAccountId.setString(1, user.getAccountTitle());
            ResultSet rs = psGetAccountId.executeQuery();

            int accountId = -1;
            if (rs.next()) {
                accountId = rs.getInt("account_id");
            } else {
                JOptionPane.showMessageDialog(null, 
                    "Account title not found in database: " + user.getAccountTitle(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                return journalId;
            }
            
            String insertDetails = "INSERT INTO journaldetails (journal_id, account_id, debit, credit) VALUES (?,?,?,?)";
            PreparedStatement psDetails = conn.prepareStatement(insertDetails);
            psDetails.setInt(1, journalId);
            psDetails.setInt(2, accountId);
            psDetails.setInt(3, user.getDebit());
            psDetails.setInt(4, user.getCredit());
            psDetails.executeUpdate();

            //JOptionPane.showMessageDialog(null, "Detail added to Journal ID: " + journalId);
            return journalId;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return journalId;
        }
    }
    
    public static List<Object[]> getAllJournalEntries() {
        List<Object[]> data = new ArrayList<>();

        try (Connection conn = ConnectionDB.getConnection()) {
            String query = "SELECT jd.journal_id, a.account_title, jd.debit, jd.credit " +
                           "FROM journaldetails jd " +
                           "JOIN accounts a ON jd.account_id = a.account_id " +
                           "ORDER BY jd.journal_id";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int journalId = rs.getInt("journal_id");
                String accountTitle = rs.getString("account_title");
                int debit = rs.getInt("debit");
                int credit = rs.getInt("credit");

                if (credit > 0) {
                    accountTitle = "     " + accountTitle;
                }

                data.add(new Object[]{journalId, accountTitle, debit, credit});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return data;
    }
    
    public static void deleteLastJournalEntry() {
        try (Connection conn = ConnectionDB.getConnection()) {

            String getLastId = "SELECT MAX(journal_id) FROM Journal";
            PreparedStatement ps = conn.prepareStatement(getLastId);
            ResultSet rs = ps.executeQuery();

            int lastId = -1;
            if (rs.next()) {
                lastId = rs.getInt(1);
            }

            if (lastId == -1) {
                JOptionPane.showMessageDialog(null, "No journal entries found.");
                return;
            }

            
            int confirm = JOptionPane.showConfirmDialog(
                    null,
                    "Do you want to delete the last journal entry with ID: " + lastId + "?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                
                String deleteDetails = "DELETE FROM journaldetails WHERE journal_id = ?";
                PreparedStatement psDetails = conn.prepareStatement(deleteDetails);
                psDetails.setInt(1, lastId);
                psDetails.executeUpdate();

           
                String deleteJournal = "DELETE FROM journal WHERE journal_id = ?";
                PreparedStatement psJournal = conn.prepareStatement(deleteJournal);
                psJournal.setInt(1, lastId);
                psJournal.executeUpdate();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static String getCompanyName() {
        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = "SELECT company_name FROM journal WHERE company_name IS NOT NULL LIMIT 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("company_name");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching company name: " + e.getMessage());
        }
        return null;
    }


    public static void saveCompanyName(String companyName) {
        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = "UPDATE journal SET company_name = ? WHERE company_name IS NULL";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, companyName);
            int updated = ps.executeUpdate();

            // If no journal yet, insert a placeholder journal to store it
            if (updated == 0) {
                String insert = "INSERT INTO journal (company_name) VALUES (?)";
              PreparedStatement psInsert = conn.prepareStatement(insert);
              psInsert.setString(1, companyName);
              psInsert.executeUpdate();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error saving company name: " + e.getMessage());
        }
    }
}
