import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.*;

public class FinancialAnalyzer {
    private List<Expenditure> expenditures;
    private List<BankAccount> bankAccounts;
    
    // Constructor
    public FinancialAnalyzer(List<Expenditure> expenditures, List<BankAccount> bankAccounts) {
        this.expenditures = expenditures;
        this.bankAccounts = bankAccounts;
    }
    
    // 1. Monthly Cash Flow Analysis
    public Map<String, Double[]> getMonthlyCashFlow() {
        Map<String, Double[]> monthlyData = new TreeMap<>();
        
        for (Expenditure exp : expenditures) {
            String monthYear = exp.getDate().format(DateTimeFormatter.ofPattern("MMM-yyyy"));
            
            monthlyData.putIfAbsent(monthYear, new Double[]{0.0, 0.0});
            Double[] amounts = monthlyData.get(monthYear);
            
            // Assuming negative amounts are expenses, positive are income
            if (exp.getAmount() < 0) {
                amounts[0] += exp.getAmount(); // Expenses
            } else {
                amounts[1] += exp.getAmount(); // Income
            }
        }
        
        return monthlyData;
    }
    
    // 2. Burn Rate Calculation (monthly average expenses)
    public double calculateBurnRate() {
        Map<String, Double[]> monthlyData = getMonthlyCashFlow();
        if (monthlyData.isEmpty()) return 0.0;
        
        double totalExpenses = 0.0;
        for (Double[] amounts : monthlyData.values()) {
            totalExpenses += Math.abs(amounts[0]); // Expenses are stored as negative
        }
        
        return totalExpenses / monthlyData.size();
    }
    
    // 3. Forecast Future Cash Needs
    public Map<String, Double> forecastCashNeeds(int monthsToProject) {
        Map<String, Double> forecast = new TreeMap<>();
        double burnRate = calculateBurnRate();
        
        LocalDate currentDate = LocalDate.now();
        for (int i = 1; i <= monthsToProject; i++) {
            LocalDate futureDate = currentDate.plusMonths(i);
            String monthYear = futureDate.format(DateTimeFormatter.ofPattern("MMM-yyyy"));
            forecast.put(monthYear, burnRate);
        }
        
        return forecast;
    }
    
    // 4. Profitability per Project/Category
    public Map<String, Double> getProfitabilityByCategory() {
        Map<String, Double> categoryProfits = new HashMap<>();
        
        for (Expenditure exp : expenditures) {
            String category = exp.getCategory();
            categoryProfits.putIfAbsent(category, 0.0);
            categoryProfits.put(category, categoryProfits.get(category) + exp.getAmount());
        }
        
        return categoryProfits;
    }
    
    // 5. Budget vs Actuals (simplified)
    public void compareBudgetVsActuals(double budget) {
        double totalExpenses = 0.0;
        for (Expenditure exp : expenditures) {
            if (exp.getAmount() < 0) { // Only count expenses
                totalExpenses += Math.abs(exp.getAmount());
            }
        }
        
        System.out.println("\n=== Budget vs Actuals ===");
        System.out.printf("Budget: GH₵%.2f%n", budget);
        System.out.printf("Actual Expenses: GH₵%.2f%n", totalExpenses);
        System.out.printf("Variance: GH₵%.2f%n", (budget - totalExpenses));
    }
    
    // 6. Building Material Price Impact Analysis
    public void analyzeMaterialImpact(String materialCategory) {
        double materialCost = 0.0;
        double otherCosts = 0.0;
        int materialTransactions = 0;
        
        for (Expenditure exp : expenditures) {
            if (exp.getCategory().equalsIgnoreCase(materialCategory)) {
                materialCost += Math.abs(exp.getAmount());
                materialTransactions++;
            } else if (exp.getAmount() < 0) {
                otherCosts += Math.abs(exp.getAmount());
            }
        }
        
        System.out.println("\n=== Material Cost Analysis ===");
        System.out.printf("Total %s costs: GH₵%.2f (%d transactions)%n", 
            materialCategory, materialCost, materialTransactions);
        System.out.printf("Percentage of total costs: %.1f%%%n", 
            (materialCost / (materialCost + otherCosts)) * 100);
    }
    
    // Helper method to display cash flow report
    public void displayCashFlowReport() {
        Map<String, Double[]> monthlyData = getMonthlyCashFlow();
        
        System.out.println("\n=== Monthly Cash Flow Report ===");
        System.out.println("Month-Year\tExpenses\tIncome\t\tNet");
        System.out.println("-----------------------------------------------");
        
        for (Map.Entry<String, Double[]> entry : monthlyData.entrySet()) {
            String month = entry.getKey();
            Double[] amounts = entry.getValue();
            double net = amounts[1] + amounts[0]; // income + expense (expense is negative)
            
            System.out.printf("%s\tGH₵%.2f\tGH₵%.2f\tGH₵%.2f%n",
                month, Math.abs(amounts[0]), amounts[1], net);
        }
        
        System.out.println("\nAverage Monthly Burn Rate: GH₵" + String.format("%.2f", calculateBurnRate()));
    }
}

// Supporting classes (these should match what your team is using)
class Expenditure {
    private String code;
    private double amount;
    private LocalDate date;
    private String phase;
    private String category;
    private String accountId;
    
    // Constructor, getters, and setters
    public LocalDate getDate() { return date; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    // ... other getters
}

class BankAccount {
    private String accountId;
    private String bankName;
    private double balance;
    private List<String> expenditureCodes;
    
    // Constructor, getters, and setters
}