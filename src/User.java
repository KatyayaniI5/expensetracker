import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.YearMonth;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String username;
    private final List<Expense> expenses;
    private final Map<String, Double> categoryBudgets;
    private double monthlyBudget;

    public User(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        this.username = username.trim();
        this.expenses = new ArrayList<>();
        this.categoryBudgets = new HashMap<>();
        this.monthlyBudget = 0.0;
    }

    public String getUsername() {
        return username;
    }

    public List<Expense> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public void addExpense(Expense expense) {
        if (expense == null) {
            throw new IllegalArgumentException("Expense cannot be null");
        }
        expenses.add(expense);
    }

    public void setCategoryBudget(String category, double budget) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        if (budget < 0) {
            throw new IllegalArgumentException("Budget cannot be negative");
        }
        categoryBudgets.put(category.trim(), budget);
    }

    public double getCategoryBudget(String category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return categoryBudgets.getOrDefault(category.trim(), 0.0);
    }

    public void setMonthlyBudget(double budget) {
        if (budget < 0) {
            throw new IllegalArgumentException("Budget cannot be negative");
        }
        this.monthlyBudget = budget;
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public double getTotalSpent() {
        return expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public double getMonthlySpent(YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException("Month cannot be null");
        }
        return expenses.stream()
                .filter(e -> YearMonth.from(e.getLocalDate()).equals(month))
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public double getCategorySpent(String category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return expenses.stream()
                .filter(e -> e.getCategory().equals(category.trim()))
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public List<Expense> getExpensesByCategory(String category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return expenses.stream()
                .filter(e -> e.getCategory().equals(category.trim()))
                .toList();
    }

    public List<Expense> getExpensesByMonth(YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException("Month cannot be null");
        }
        return expenses.stream()
                .filter(e -> YearMonth.from(e.getLocalDate()).equals(month))
                .toList();
    }

    public Map<String, Double> getCategoryBudgets() {
        return Collections.unmodifiableMap(categoryBudgets);
    }
}