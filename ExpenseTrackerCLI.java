import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExpenseTrackerCLI {
    private static final String EXPENDITURE_FILE = "expenditures.txt";
    private static final String CATEGORY_FILE = "categories.txt";

    static List<Expenditure> expenditures = new ArrayList<>();
    static Map<String, Category> categories = new HashMap<>();
    static Scanner scanner = new Scanner(System.in);

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";

    public static void main(String[] args) {
        System.out.println(GREEN + "Akwaaba to Nkwa-Boa! (Your Expenditure Tracking App)" + RESET);

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        System.out.println("Welcome, " + name + "!");

        File categoryFile = new File(CATEGORY_FILE);
        if (categoryFile.exists()) {
            System.out.println("\nCategories previously saved:");
            printFileContents(categoryFile);
        } else {
            System.out.println("\nNo category file found. A new one will be created.");
        }

        File expenditureFile = new File(EXPENDITURE_FILE);
        if (expenditureFile.exists()) {
            System.out.println("\nExpenditures previously saved:");
            printFileContents(expenditureFile);
        } else {
            System.out.println("\nNo expenditure file found. A new one will be created.");
        }

        loadData();
        mainMenu();
        saveData();
        System.out.println("Exiting... Data saved successfully!");
    }

    private static void printFileContents(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("  - " + line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + file.getName());
        }
    }

    static void mainMenu() {
        while (true) {
            System.out.println("\nMain Menu");
            System.out.println("1. Add Expenditure");
            System.out.println("2. View Expenditures");
            System.out.println("3. Search Expenditures");
            System.out.println("4. Manage Categories");
            System.out.println("5. Generate Report");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            System.out.flush();

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addExpenditure();
                case "2" -> viewExpenditures();
                case "3" -> searchExpenditures();
                case "4" -> manageCategories();
                case "5" -> reportTotal();
                case "6" -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter a number from 1 to 6.");
            }
        }
    }

    static void addExpenditure() {
        System.out.println("\nAdd New Expenditure");
        try {
            System.out.print("Enter amount: ");
            double amount = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter category ID: ");
            String catId = scanner.nextLine().trim();
            if (!categories.containsKey(catId)) {
                System.out.print("Category not found. Add it now? (y/n): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                    addCategoryWithId(catId);
                } else {
                    System.out.println("Expenditure not recorded due to missing category.");
                    return;
                }
            }

            System.out.print("Enter date (YYYY-MM-DD): ");
            String date = scanner.nextLine().trim();
            if (!isValidDate(date)) {
                System.out.println("Invalid date format!");
                return;
            }

            System.out.print("Enter payment method (Cash/Bank/MoMo): ");
            String paymentMethod = scanner.nextLine().trim();

            System.out.print("Enter vendor name: ");
            String vendor = scanner.nextLine().trim();

            String id = "EXP" + (expenditures.size() + 1);
            expenditures.add(new Expenditure(id, amount, catId, date, paymentMethod, vendor));
            System.out.println("Expenditure recorded successfully!");
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number for amount.");
        }
    }

    static void viewExpenditures() {
        System.out.println("\nAll Expenditures");
        if (expenditures.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        expenditures.forEach(System.out::println);
    }

    static void searchExpenditures() {
        System.out.print("Search by category ID or vendor: ");
        String keyword = scanner.nextLine().trim().toLowerCase();
        List<Expenditure> results = new ArrayList<>();

        for (Expenditure e : expenditures) {
            if (e.categoryId.toLowerCase().contains(keyword) || e.vendor.toLowerCase().contains(keyword)) {
                results.add(e);
            }
        }

        if (results.isEmpty()) {
            System.out.println("No matches found.");
        } else {
            results.forEach(System.out::println);
        }
    }

    static void manageCategories() {
        while (true) {
            System.out.println("\nCategory Menu");
            System.out.println("1. View Categories");
            System.out.println("2. Add Category");
            System.out.println("3. Back");
            System.out.print("Choose option: ");
            System.out.flush();

            String option = scanner.nextLine().trim();
            switch (option) {
                case "1" -> viewCategories();
                case "2" -> addCategory();
                case "3" -> {
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    static void viewCategories() {
        if (categories.isEmpty()) {
            System.out.println("No categories yet.");
            return;
        }
        categories.values().forEach(System.out::println);
    }

    static void addCategory() {
        System.out.print("Enter category ID: ");
        String id = scanner.nextLine().trim();
        if (categories.containsKey(id)) {
            System.out.println("Category already exists.");
            return;
        }
        addCategoryWithId(id);
    }

    static void addCategoryWithId(String id) {
        System.out.print("Enter category name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter budget limit: ");
        try {
            double limit = Double.parseDouble(scanner.nextLine());
            categories.put(id, new Category(id, name, limit));
            System.out.println("Category added.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number for budget.");
        }
    }

    static void reportTotal() {
        double total = expenditures.stream().mapToDouble(e -> e.amount).sum();
        System.out.printf("Total Expenditure: GHS %.2f\n", total);
    }

    static void loadData() {
        loadCategories();
        loadExpenditures();
    }

    static void saveData() {
        saveCategories();
        saveExpenditures();
    }

    static void loadExpenditures() {
        long start = System.currentTimeMillis();
        try (BufferedReader br = new BufferedReader(new FileReader(EXPENDITURE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                Expenditure e = Expenditure.fromString(line);
                if (e != null) expenditures.add(e);
            }
            System.out.printf("Loaded expenditures in %d ms\n", System.currentTimeMillis() - start);
        } catch (IOException e) {
            System.out.println("No expenditure file found. A new one will be created.");
        }
    }

    static void saveExpenditures() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(EXPENDITURE_FILE))) {
            for (Expenditure e : expenditures) {
                pw.println(e.toDataString());
            }
        } catch (IOException e) {
            System.out.println("Failed to save expenditures.");
        }
    }

    static void loadCategories() {
        try (BufferedReader br = new BufferedReader(new FileReader(CATEGORY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                Category c = Category.fromString(line);
                if (c != null) categories.put(c.id, c);
            }
        } catch (IOException e) {
            System.out.println("No category file found. Starting with empty category list.");
        }
    }

    static void saveCategories() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CATEGORY_FILE))) {
            for (Category c : categories.values()) {
                pw.println(c.toDataString());
            }
        } catch (IOException e) {
            System.out.println("Failed to save categories.");
        }
    }

    static boolean isValidDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    static class Expenditure {
        String id, categoryId, date, paymentMethod, vendor;
        double amount;

        public Expenditure(String id, double amount, String categoryId, String date, String paymentMethod, String vendor) {
            this.id = id;
            this.amount = amount;
            this.categoryId = categoryId;
            this.date = date;
            this.paymentMethod = paymentMethod;
            this.vendor = vendor;
        }

        public static Expenditure fromString(String line) {
            try {
                String[] parts = line.split("\\|");
                return new Expenditure(parts[0], Double.parseDouble(parts[1]), parts[2], parts[3], parts[4], parts[5]);
            } catch (Exception e) {
                return null;
            }
        }

        public String toDataString() {
            return String.join("|", id, String.valueOf(amount), categoryId, date, paymentMethod, vendor);
        }

        public String toString() {
            return String.format("ID: %s | GHS %.2f | Cat: %s | %s | %s | Vendor: %s",
                    id, amount, categoryId, date, paymentMethod, vendor);
        }
    }

    static class Category {
        String id, name;
        double budgetLimit;

        public Category(String id, String name, double budgetLimit) {
            this.id = id;
            this.name = name;
            this.budgetLimit = budgetLimit;
        }

        public static Category fromString(String line) {
            try {
                String[] parts = line.split("\\|");
                return new Category(parts[0], parts[1], Double.parseDouble(parts[2]));
            } catch (Exception e) {
                return null;
            }
        }

        public String toDataString() {
            return String.join("|", id, name, String.valueOf(budgetLimit));
        }

        public String toString() {
            return String.format("ID: %s | %s | Limit: GHS %.2f", id, name, budgetLimit);
        }
    }
}
