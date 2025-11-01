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

public class BalanceSheetDA {
    
    public static class AccountBalance {
        public String accountTitle;
        public double balance;

        public AccountBalance(String accountTitle, double balance) {
            this.accountTitle = accountTitle;
            this.balance = balance;
        }
    }

    public static Map<String, List<AccountBalance>> getBalanceSheetData() {
        Map<String, List<AccountBalance>> map = new HashMap<>();
        map.put("CurrentAsset", new ArrayList<>());
        map.put("NoncurrentAsset", new ArrayList<>());
        map.put("Liabilities", new ArrayList<>());
        map.put("Equity", new ArrayList<>());

        try (Connection conn = ConnectionDB.getConnection()) {
            String query = """
                SELECT a.account_title, SUM(jd.debit) AS total_debit, SUM(jd.credit) AS total_credit, a.account_type
                FROM journaldetails jd
                JOIN accounts a ON jd.account_id = a.account_id
                GROUP BY a.account_title, a.account_type
            """;

            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            double prepaidTotal = 0;
            double equipmentTotal = 0;
            
            while (rs.next()) {
                String title = rs.getString("account_title");
                String type = rs.getString("account_type"); 
                double debit = rs.getDouble("total_debit");
                double credit = rs.getDouble("total_credit");
                double balance = Math.abs(debit - credit);

                
                if (title.toLowerCase().contains("rent expense") || title.toLowerCase().contains("salary expense") || title.toLowerCase().contains("utility expense") ||  title.toLowerCase().contains("transportation expense ") ||  title.toLowerCase().contains("freight in") ||  title.toLowerCase().contains("freight out") ) {
                    prepaidTotal += balance;  
                    continue;
                }

                
                if (title.toLowerCase().contains("equipment") || title.toLowerCase().contains("furniture") || title.toLowerCase().contains("machineries")) {
                    equipmentTotal += balance;
                    continue;
                }

                AccountBalance entry = new AccountBalance(title, balance);
                switch (type.toLowerCase()) {
                    case "current asset" -> map.get("CurrentAsset").add(entry);
                    case "noncurrent asset" -> map.get("NoncurrentAsset").add(entry);
                    case "liabilities" -> map.get("Liabilities").add(entry);
                    case "equity" -> map.get("Equity").add(entry);
                }
            }

           
            if (prepaidTotal > 0)
                map.get("CurrentAsset").add(new AccountBalance("Prepaid Expense", prepaidTotal));
            if (equipmentTotal > 0)
                map.get("NoncurrentAsset").add(new AccountBalance("Equipments", equipmentTotal));
            if (dao.IncomeStatementDB.netIncome != 0) {
                double netIncome = dao.IncomeStatementDB.netIncome;
                if (netIncome > 0)
                    map.get("Equity").add(new AccountBalance("Net Income", netIncome));
                else
                    map.get("Equity").add(new AccountBalance("(less) Net Loss", Math.abs(netIncome) * -1));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading balance sheet data: " + e.getMessage());
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
