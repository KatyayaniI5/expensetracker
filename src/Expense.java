import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Expense implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private final String date;
    private final String category;
    private final double amount;
    private final String description;

    public Expense(String date, String category, double amount, String description) {
        if (date == null || !isValidDate(date)) {
            throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        
        this.date = date;
        this.category = category.trim();
        this.amount = amount;
        this.description = description.trim();
    }

    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getLocalDate() {
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    @Override
    public String toString() {
        return String.format("%-10s | %-10s | $%8.2f | %s", 
            date, category, amount, description);
    }
}