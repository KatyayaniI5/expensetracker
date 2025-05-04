// Global variables
let currentUser = null;
const STORAGE_KEY = 'expenseTrackerData';

// DOM Elements
const loginBtn = document.getElementById('loginBtn');
const usernameInput = document.getElementById('username');
const expenseForm = document.getElementById('expenseForm');
const expensesList = document.getElementById('expensesList');
const filterCategory = document.getElementById('filterCategory');
const filterMonth = document.getElementById('filterMonth');
const totalSpentElement = document.getElementById('totalSpent');
const monthlyBudgetElement = document.getElementById('monthlyBudget');
const remainingBudgetElement = document.getElementById('remainingBudget');

// Event Listeners
loginBtn.addEventListener('click', handleLogin);
expenseForm.addEventListener('submit', handleAddExpense);
filterCategory.addEventListener('change', updateExpensesList);
filterMonth.addEventListener('change', updateExpensesList);

// Initialize
loadData();

// Functions
function loadData() {
    const data = localStorage.getItem(STORAGE_KEY);
    if (data) {
        try {
            const parsedData = JSON.parse(data);
            if (parsedData.currentUser) {
                currentUser = parsedData.currentUser;
                updateUI();
            }
        } catch (error) {
            console.error('Error loading data:', error);
        }
    }
}

function saveData() {
    if (currentUser) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify({ currentUser }));
    }
}

function handleLogin() {
    const username = usernameInput.value.trim();
    if (!username) {
        alert('Please enter a username');
        return;
    }

    // Check if user exists in localStorage
    const data = localStorage.getItem(STORAGE_KEY);
    let existingData = {};
    
    if (data) {
        try {
            existingData = JSON.parse(data);
        } catch (error) {
            console.error('Error parsing data:', error);
        }
    }

    if (existingData.currentUser && existingData.currentUser.username === username) {
        currentUser = existingData.currentUser;
    } else {
        // Create new user
        currentUser = {
            username,
            expenses: [],
            monthlyBudget: 0,
            categoryBudgets: {}
        };
    }

    updateUI();
    saveData();
}

function handleAddExpense(event) {
    event.preventDefault();
    if (!currentUser) {
        alert('Please login first');
        return;
    }

    const date = document.getElementById('date').value;
    const category = document.getElementById('category').value.trim();
    const amount = parseFloat(document.getElementById('amount').value);
    const description = document.getElementById('description').value.trim();

    if (!date || !category || isNaN(amount) || amount <= 0) {
        alert('Please fill in all required fields correctly');
        return;
    }

    const expense = {
        date,
        category,
        amount,
        description
    };

    currentUser.expenses.push(expense);
    saveData();
    updateUI();
    expenseForm.reset();
}

function updateUI() {
    updateSummary();
    updateExpensesList();
    updateFilters();
}

function updateSummary() {
    const totalSpent = currentUser.expenses.reduce((sum, expense) => sum + expense.amount, 0);
    const monthlyBudget = currentUser.monthlyBudget || 0;
    const remaining = monthlyBudget - totalSpent;

    totalSpentElement.textContent = `$${totalSpent.toFixed(2)}`;
    monthlyBudgetElement.textContent = `$${monthlyBudget.toFixed(2)}`;
    remainingBudgetElement.textContent = `$${remaining.toFixed(2)}`;
    remainingBudgetElement.style.color = remaining < 0 ? 'var(--error-color)' : 'var(--success-color)';
}

function updateExpensesList() {
    const categoryFilter = filterCategory.value;
    const monthFilter = filterMonth.value;

    let filteredExpenses = [...currentUser.expenses];

    if (categoryFilter) {
        filteredExpenses = filteredExpenses.filter(expense => expense.category === categoryFilter);
    }

    if (monthFilter) {
        filteredExpenses = filteredExpenses.filter(expense => {
            const expenseMonth = expense.date.substring(0, 7); // YYYY-MM
            return expenseMonth === monthFilter;
        });
    }

    expensesList.innerHTML = '';
    filteredExpenses.forEach(expense => {
        const expenseElement = document.createElement('div');
        expenseElement.className = 'expense-item';
        expenseElement.innerHTML = `
            <span>${expense.date}</span>
            <span>${expense.category}</span>
            <span>$${expense.amount.toFixed(2)}</span>
            <span>${expense.description || ''}</span>
        `;
        expensesList.appendChild(expenseElement);
    });
}

function updateFilters() {
    // Update category filter
    const categories = [...new Set(currentUser.expenses.map(expense => expense.category))];
    filterCategory.innerHTML = '<option value="">All Categories</option>';
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category;
        option.textContent = category;
        filterCategory.appendChild(option);
    });

    // Update month filter
    const months = [...new Set(currentUser.expenses.map(expense => expense.date.substring(0, 7)))];
    filterMonth.innerHTML = '<option value="">All Months</option>';
    months.forEach(month => {
        const option = document.createElement('option');
        option.value = month;
        option.textContent = new Date(month + '-01').toLocaleDateString('en-US', { year: 'numeric', month: 'long' });
        filterMonth.appendChild(option);
    });
} 