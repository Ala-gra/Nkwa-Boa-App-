import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FinancialAnalyzer {
    private List<Expenditure> expenditures;
    private List<BankAccount> bankAccounts;

    // Constructor
    public FinancialAnalyzer(List<Expenditure> expenditures, List<BankAccount> bankAccounts) {
        this.expenditures = expenditures != null ? expenditures : new ArrayList<>();
        this.bankAccounts = bankAccounts != null ? bankAccounts : new ArrayList<>();
    }

    // 1. Monthly Cash Flow Analysis
    public Map<String, double[]> getMonthlyCashFlow() {
        Map<String, double[]> monthlyData = new TreeMap<>();
        
        for (Expenditure exp : expenditures) {
            if (exp == null || exp.getDate() == null) continue;
            
            String monthYear = exp.getDate().format(DateTimeFormatter.ofPattern("MMM-yyyy"));
            monthlyData.putIfAbsent(monthYear, new double[]{0.0, 0.0});
            double[] amounts = monthlyData.get(monthYear);
            
            if (exp.getAmount() < 0) {
                amounts[0] += Math.abs(exp.getAmount()); // Expenses
            } else {
                amounts[1] += exp.getAmount(); // Income
            }
        }
        
        return monthlyData;
    }

    // 2. Burn Rate Calculation (monthly average expenses)
    public double calculateBurnRate() {
        Map<String, double[]> monthlyData = getMonthlyCashFlow();
        if (monthlyData.isEmpty()) return 0.0;
        
        double totalExpenses = 0.0;
        for (double[] amounts : monthlyData.values()) {
            totalExpenses += amounts[0]; // Expenses are stored as positive values
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

    // 4. Profitability per Category
    public Map<String, Double> getProfitabilityByCategory() {
        Map<String, Double> categoryProfits = new HashMap<>();
        
        for (Expenditure exp : expenditures) {
            if (exp == null || exp.getCategory() == null) continue;
            
            String category = exp.getCategory();
            categoryProfits.putIfAbsent(category, 0.0);
            categoryProfits.put(category, categoryProfits.get(category) + exp.getAmount());
        }
        
        return categoryProfits;
    }

    // 5. Budget vs Actuals
    public String compareBudgetVsActuals(double budget) {
        double totalExpenses = 0.0;
        for (Expenditure exp : expenditures) {
            if (exp != null && exp.getAmount() < 0) {
                totalExpenses += Math.abs(exp.getAmount());
            }
        }
        
        double variance = budget - totalExpenses;
        return String.format("Budget: GH₵%.2f\nActual Expenses: GH₵%.2f\nVariance: GH₵%.2f (%s)",
                budget, totalExpenses, Math.abs(variance),
                variance >= 0 ? "Under Budget" : "Over Budget");
    }

    // 6. Material Cost Impact Analysis
    public String analyzeMaterialImpact(String materialCategory) {
        if (materialCategory == null) return "Invalid material category";
        
        double materialCost = 0.0;
        double otherCosts = 0.0;
        int materialTransactions = 0;
        
        for (Expenditure exp : expenditures) {
            if (exp == null || exp.getAmount() >= 0) continue;
            
            if (materialCategory.equalsIgnoreCase(exp.getCategory())) {
                materialCost += Math.abs(exp.getAmount());
                materialTransactions++;
            } else {
                otherCosts += Math.abs(exp.getAmount());
            }
        }
        
        double totalCosts = materialCost + otherCosts;
        double percentage = totalCosts > 0 ? (materialCost / totalCosts) * 100 : 0;
        
        return String.format("Material: %s\nTotal Cost: GH₵%.2f (%d transactions)\n" +
                           "Percentage of Total Costs: %.1f%%",
                materialCategory, materialCost, materialTransactions, percentage);
    }

    // 7. Display Cash Flow Report
    public String generateCashFlowReport() {
        Map<String, double[]> monthlyData = getMonthlyCashFlow();
        if (monthlyData.isEmpty()) return "No expenditure data available";
        
        StringBuilder report = new StringBuilder();
        report.append("Monthly Cash Flow Report\n");
        report.append("-----------------------------------------------\n");
        report.append("Month-Year\tExpenses\tIncome\t\tNet\n");
        report.append("-----------------------------------------------\n");
        
        for (Map.Entry<String, double[]> entry : monthlyData.entrySet()) {
            String month = entry.getKey();
            double[] amounts = entry.getValue();
            double net = amounts[1] - amounts[0];
            
            report.append(String.format("%s\tGH₵%.2f\tGH₵%.2f\tGH₵%.2f%n",
                    month, amounts[0], amounts[1], net));
        }
        
        report.append("\nAverage Monthly Burn Rate: GH₵").append(String.format("%.2f", calculateBurnRate()));
        return report.toString();
    }
}