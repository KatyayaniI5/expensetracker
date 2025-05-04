import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ExpenseTrackerApp {
    private Scanner scanner = new Scanner(System.in);
    private User currentUser;
    private static final String DATA_DIR = "data/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public ExpenseTrackerApp() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            System.err.println("Failed to create data directory");
        }
    }

    public void run() {
        System.out.println("=== Expense Tracker ===");
        System.out.print("Enter username to login: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty");
            return;
        }
        currentUser = loadUser(username);
        System.out.println("Welcome, " + currentUser.getUsername() + "!");

        while (true) {
            displayMainMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        addExpense();
                        break;
                    case "2":
                        viewExpenses();
                        break;
                    case "3":
                        setBudget();
                        break;
                    case "4":
                        viewSummary();
                        break;
                    case "5":
                        filterExpenses();
                        break;
                    case "6":
                        saveUser(currentUser);
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void displayMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Add Expense");
        System.out.println("2. View Expenses");
        System.out.println("3. Set Budget");
        System.out.println("4. View Summary");
        System.out.println("5. Filter Expenses");
        System.out.println("6. Exit");
        System.out.print("Choose an option: ");
    }

    private void addExpense() {
        System.out.println("\n=== Add New Expense ===");
        
        String date = getValidDate();
        String category = getValidCategory();
        double amount = getValidAmount();
        System.out.print("Enter description (optional): ");
        String description = scanner.nextLine().trim();

        Expense expense = new Expense(date, category, amount, description);
        currentUser.addExpense(expense);
        System.out.println("Expense added successfully!");
    }

    private String getValidDate() {
        while (true) {
            System.out.print("Enter date (YYYY-MM-DD): ");
            String date = scanner.nextLine().trim();
            try {
                LocalDate.parse(date, DATE_FORMATTER);
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD");
            }
        }
    }

    private String getValidCategory() {
        while (true) {
            System.out.print("Enter category: ");
            String category = scanner.nextLine().trim();
            if (!category.isEmpty()) {
                return category;
            }
            System.out.println("Category cannot be empty");
        }
    }

    private double getValidAmount() {
        while (true) {
            System.out.print("Enter amount: ");
            try {
                double amount = Double.parseDouble(scanner.nextLine().trim());
                if (amount > 0) {
                    return amount;
                }
                System.out.println("Amount must be greater than 0");
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a number");
            }
        }
    }

    private void viewExpenses() {
        System.out.println("\n=== View Expenses ===");
        List<Expense> expenses = currentUser.getExpenses();
        
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded yet.");
            return;
        }

        System.out.println("Date       | Category    | Amount    | Description");
        System.out.println("------------------------------------------------");
        for (Expense e : expenses) {
            System.out.println(e);
        }
    }

    private void setBudget() {
        System.out.println("\n=== Set Budget ===");
        System.out.println("1. Set Monthly Budget");
        System.out.println("2. Set Category Budget");
        System.out.print("Choose an option: ");
        
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                System.out.print("Enter monthly budget: ");
                double monthlyBudget = getValidAmount();
                currentUser.setMonthlyBudget(monthlyBudget);
                System.out.println("Monthly budget set successfully!");
                break;
            case "2":
                String category = getValidCategory();
                System.out.print("Enter budget for " + category + ": ");
                double categoryBudget = getValidAmount();
                currentUser.setCategoryBudget(category, categoryBudget);
                System.out.println("Category budget set successfully!");
                break;
            default:
                System.out.println("Invalid option");
        }
    }

    private void viewSummary() {
        System.out.println("\n=== Expense Summary ===");
        System.out.printf("Total Spent: $%.2f%n", currentUser.getTotalSpent());
        
        if (currentUser.getMonthlyBudget() > 0) {
            double monthlySpent = currentUser.getMonthlySpent(YearMonth.now());
            System.out.printf("Monthly Budget: $%.2f%n", currentUser.getMonthlyBudget());
            System.out.printf("Monthly Spent: $%.2f%n", monthlySpent);
            System.out.printf("Remaining Budget: $%.2f%n", 
                currentUser.getMonthlyBudget() - monthlySpent);
        }

        System.out.println("\nCategory-wise Summary:");
        Map<String, Double> categoryBudgets = currentUser.getCategoryBudgets();
        for (String category : currentUser.getExpenses().stream()
                .map(Expense::getCategory)
                .distinct()
                .toList()) {
            double spent = currentUser.getCategorySpent(category);
            double budget = categoryBudgets.getOrDefault(category, 0.0);
            System.out.printf("%s: $%.2f spent", category, spent);
            if (budget > 0) {
                System.out.printf(" (Budget: $%.2f, Remaining: $%.2f)%n", 
                    budget, budget - spent);
            } else {
                System.out.println();
            }
        }
    }

    private void filterExpenses() {
        System.out.println("\n=== Filter Expenses ===");
        System.out.println("1. By Category");
        System.out.println("2. By Month");
        System.out.print("Choose an option: ");
        
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                String category = getValidCategory();
                List<Expense> categoryExpenses = currentUser.getExpensesByCategory(category);
                displayFilteredExpenses(categoryExpenses, "Category: " + category);
                break;
            case "2":
                System.out.print("Enter month (YYYY-MM): ");
                String monthStr = scanner.nextLine().trim();
                try {
                    YearMonth month = YearMonth.parse(monthStr);
                    List<Expense> monthExpenses = currentUser.getExpensesByMonth(month);
                    displayFilteredExpenses(monthExpenses, "Month: " + monthStr);
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid month format. Please use YYYY-MM");
                }
                break;
            default:
                System.out.println("Invalid option");
        }
    }

    private void displayFilteredExpenses(List<Expense> expenses, String filter) {
        if (expenses.isEmpty()) {
            System.out.println("No expenses found for " + filter);
            return;
        }

        System.out.println("\nExpenses for " + filter);
        System.out.println("Date       | Category    | Amount    | Description");
        System.out.println("------------------------------------------------");
        for (Expense e : expenses) {
            System.out.println(e);
        }
    }

    private User loadUser(String username) {
        File file = new File(DATA_DIR + username + ".dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (User) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading user data. Creating new user.");
            }
        }
        return new User(username);
    }

    private void saveUser(User user) {
        File file = new File(DATA_DIR + user.getUsername() + ".dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(user);
        } catch (IOException e) {
            System.out.println("Error saving user data: " + e.getMessage());
        }
    }
}