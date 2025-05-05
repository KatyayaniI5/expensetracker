from django import forms
from .models import Expense, UserProfile

class ExpenseForm(forms.ModelForm):
    class Meta:
        model = Expense
        fields = ['date', 'category', 'amount', 'description']
        widgets = {
            'date': forms.DateInput(attrs={'type': 'date'}),
            'amount': forms.NumberInput(attrs={'step': '0.01'}),
        }

class UserProfileForm(forms.ModelForm):
    class Meta:
        model = UserProfile
        fields = ['monthly_budget']
        widgets = {
            'monthly_budget': forms.NumberInput(attrs={'step': '0.01'}),
        } 