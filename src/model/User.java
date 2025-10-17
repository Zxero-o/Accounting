/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import javax.swing.*;
/**
 *
 * @author marionne pascual
 */
public class User {
    private String accountTitle;
    private int debit;
    private int credit;
    private static final String COMPANY_FILE = "company.txt";

    public String getAccountTitle() {
        return accountTitle;
    }

    public void setAccountTitle(String accountTitle) {
        this.accountTitle = accountTitle;
    }

    public int getDebit() {
        return debit;
    }

    public void setDebit(int debit) {
        this.debit = debit;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }
    
    public void saveCompanyName(String name) {
        try (java.io.FileWriter writer = new java.io.FileWriter(COMPANY_FILE)) {
            writer.write(name);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error saving company name: " + e.getMessage());
        }
    }
    
    public String loadCompanyName() {
        try (java.util.Scanner scanner = new java.util.Scanner(new java.io.File(COMPANY_FILE))) {
            if (scanner.hasNextLine()) {
                return scanner.nextLine();
            }
        } catch (Exception e) {
            // no file yet, ignore
        }
        return null;
    }
    
}
