/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author marionne pascual
 */
import java.sql.*;
import java.util.*;
import javax.swing.JOptionPane;

public class IncomeStatementDB {
    public static double netIncome = 0;
    
     public static class AccountBalance {
        public String accountTitle;
        public double balance;

        public AccountBalance(String accountTitle, double balance) {
            this.accountTitle = accountTitle;
            this.balance = balance;
        }
    }

    public static Map<String, List<AccountBalance>> getIncomeStatementData() {
        Map<String, List<AccountBalance>> map = new HashMap<>();
        map.put("Income", new ArrayList<>());
        map.put("COGS", new ArrayList<>());
        map.put("Expense", new ArrayList<>());

        try (Connection conn = ConnectionDB.getConnection()) {
            String query = """
                SELECT a.account_title, SUM(jd.debit) AS total_debit, SUM(jd.credit) AS total_credit, a.account_type
                FROM journaldetails jd
                JOIN accounts a ON jd.account_id = a.account_id
                WHERE a.account_type IN ('Income', 'Expense', 'COGS')
                GROUP BY a.account_title, a.account_type
            """;

            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String title = rs.getString("account_title");
            String type = rs.getString("account_type");
            double debit = rs.getDouble("total_debit");
            double credit = rs.getDouble("total_credit");

            double balance;
            if (type.equalsIgnoreCase("Income")) {
                balance = credit - debit; 
                if (title.toLowerCase().contains("sales returns")) {
                    title = "(less) " + title;
                    balance *= -1; 
                }
                map.get("Income").add(new AccountBalance(title, balance));
            } else { 
                balance = debit - credit;
                if (title.toLowerCase().contains("cost of goods sold")) {
                    title = "(less) " + title;
                    map.get("COGS").add(new AccountBalance(title, balance));
                } else {
                    map.get("Expense").add(new AccountBalance(title, balance));
                }
            }
        }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading income statement: " + e.getMessage());
        }
        return map;
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
    
}
