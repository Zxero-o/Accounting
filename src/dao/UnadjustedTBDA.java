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

public class UnadjustedTBDA {
     public static class AccountTotal {
        public String accountTitle;
        public double debitTotal;
        public double creditTotal;

        public AccountTotal(String accountTitle, double debitTotal, double creditTotal) {
            this.accountTitle = accountTitle;
            this.debitTotal = debitTotal;
            this.creditTotal = creditTotal;
        }
    }

    public static List<AccountTotal> getTrialBalance() {
        List<AccountTotal> list = new ArrayList<>();

        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = """
                         SELECT a.account_title, SUM(jd.debit) AS total_debit, SUM(jd.credit) AS total_credit
                         FROM journaldetails jd
                         JOIN accounts a ON jd.account_id = a.account_id
                         GROUP BY a.account_title;""";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String title = rs.getString("account_title");
                double debit = rs.getDouble("total_debit");
                double credit = rs.getDouble("total_credit");

                list.add(new AccountTotal(title, debit, credit));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return list;
    }
    
    public static String getCompanyName(){
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
