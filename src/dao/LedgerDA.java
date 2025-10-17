/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;


import java.sql.*;
import java.util.*;
import javax.swing.JOptionPane;

/**
 *
 * @author marionne pascual
 */
public class LedgerDA {
    
     public static class LedgerEntry {
        public String accountTitle;
        public double totalDebit;
        public double totalCredit;
        public List<double[]> transactions = new ArrayList<>();
    }
     
     public static String getCompanyName() {
    String companyName = "";
    try (Connection con = ConnectionDB.getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT company_name FROM journal LIMIT 1");
         ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            companyName = rs.getString("company_name");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error fetching company name: " + e.getMessage());
    }
    return companyName;
    }
     
    public static List<LedgerEntry> getLedgerData() {
    List<LedgerEntry> ledgerList = new ArrayList<>();

    try (Connection conn = ConnectionDB.getConnection()) {
        String query = """
            SELECT a.account_title, jd.debit, jd.credit
            FROM journaldetails jd
            JOIN accounts a ON jd.account_id = a.account_id
        """;

        PreparedStatement ps = conn.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        Map<String, LedgerEntry> map = new LinkedHashMap<>();

        while (rs.next()) {
            String title = rs.getString("account_title");
            double debit = rs.getDouble("debit");
            double credit = rs.getDouble("credit");

            LedgerEntry entry = map.getOrDefault(title, new LedgerEntry());
            entry.accountTitle = title;
            entry.transactions.add(new double[]{debit, credit});
            map.put(title, entry);
        }

        // Compute totals per account
        for (LedgerEntry e : map.values()) {
            double totalDebit = 0, totalCredit = 0;
            for (double[] t : e.transactions) {
                totalDebit += t[0];
                totalCredit += t[1];
            }
            e.totalDebit = totalDebit;
            e.totalCredit = totalCredit;
            ledgerList.add(e);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, e.getMessage());
    }
    return ledgerList;
    }

}
