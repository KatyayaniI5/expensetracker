from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from django.contrib.auth import login, logout
from django.contrib.auth.forms import AuthenticationForm, UserCreationForm
from django.contrib import messages
from django.db.models import Sum
from django.utils import timezone
from .models import Expense, UserProfile
from .forms import ExpenseForm, UserProfileForm
from datetime import datetime

def login_view(request):
    if request.method == 'POST':
        form = AuthenticationForm(request, data=request.POST)
        if form.is_valid():
            user = form.get_user()
            login(request, user)
            messages.success(request, 'Successfully logged in!')
            return redirect('dashboard')
    else:
        form = AuthenticationForm()
    return render(request, 'registration/login.html', {'form': form})

def register(request):
    if request.method == 'POST':
        form = UserCreationForm(request.POST)
        if form.is_valid():
            user = form.save()
            UserProfile.objects.create(user=user)
            messages.success(request, 'Account created successfully! Please login.')
            return redirect('login')
    else:
        form = UserCreationForm()
    return render(request, 'registration/register.html', {'form': form})

@login_required
def dashboard(request):
    # Get filter parameters
    category = request.GET.get('category')
    month = request.GET.get('month')
    
    # Base queryset
    expenses = Expense.objects.filter(user=request.user)
    
    # Apply filters
    if category:
        expenses = expenses.filter(category=category)
    if month:
        try:
            month_date = datetime.strptime(month, '%Y-%m')
            expenses = expenses.filter(date__year=month_date.year, date__month=month_date.month)
        except ValueError:
            pass
    
    # Calculate totals
    total_spent = expenses.aggregate(total=Sum('amount'))['total'] or 0
    monthly_budget = request.user.userprofile.monthly_budget
    remaining_budget = monthly_budget - total_spent
    
    # Calculate budget percentage
    budget_percentage = (total_spent / monthly_budget * 100) if monthly_budget > 0 else 0
    
    # Get category data for chart
    category_data = expenses.values('category').annotate(total=Sum('amount'))
    category_labels = [item['category'] for item in category_data]
    category_amounts = [float(item['total']) for item in category_data]
    
    context = {
        'expenses': expenses.order_by('-date'),
        'total_spent': total_spent,
        'remaining_budget': remaining_budget,
        'budget_percentage': min(budget_percentage, 100),  # Cap at 100%
        'category_labels': category_labels,
        'category_data': category_amounts,
        'monthly_budget': monthly_budget,
        'profile_form': UserProfileForm(instance=request.user.userprofile),
    }
    return render(request, 'expenses/dashboard.html', context)

@login_required
def add_expense(request):
    if request.method == 'POST':
        form = ExpenseForm(request.POST)
        if form.is_valid():
            expense = form.save(commit=False)
            expense.user = request.user
            expense.save()
            messages.success(request, 'Expense added successfully!')
            return redirect('dashboard')
    else:
        form = ExpenseForm()
    return render(request, 'expenses/add_expense.html', {'form': form})

@login_required
def update_profile(request):
    if request.method == 'POST':
        form = UserProfileForm(request.POST, instance=request.user.userprofile)
        if form.is_valid():
            form.save()
            messages.success(request, 'Profile updated successfully!')
            return redirect('dashboard')
    else:
        form = UserProfileForm(instance=request.user.userprofile)
    return render(request, 'expenses/update_profile.html', {'form': form})

def logout_view(request):
    logout(request)
    messages.info(request, 'You have been logged out.')
    return redirect('login')

@login_required
def reset_month(request):
    if request.method == 'POST':
        now = datetime.now()
        # Delete all expenses for the current month for this user
        Expense.objects.filter(
            user=request.user,
            date__year=now.year,
            date__month=now.month
        ).delete()
        # Reset the monthly budget to 0
        profile = request.user.userprofile
        profile.monthly_budget = 0  # or any default value you want
        profile.save()
        messages.success(request, "All expenses and monthly budget for this month have been reset!")
    return redirect('dashboard')
