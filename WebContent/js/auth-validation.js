/**
 * Client-side Form Validation for Authentication Views
 * Smart E-Book Management System
 */

document.addEventListener('DOMContentLoaded', () => {
    
    // Register Form Validation
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        const nameInput = document.getElementById('name');
        const emailInput = document.getElementById('email');
        const passwordInput = document.getElementById('password');
        const confirmPasswordInput = document.getElementById('confirmPassword');

        registerForm.addEventListener('submit', (event) => {
            let isValid = true;
            clearAllErrors(registerForm);

            // Name Validation
            if (!nameInput.value.trim()) {
                showError(nameInput, 'Full name is required.');
                isValid = false;
            }

            // Email Validation
            const emailValue = emailInput.value.trim();
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailValue) {
                showError(emailInput, 'Email address is required.');
                isValid = false;
            } else if (!emailRegex.test(emailValue)) {
                showError(emailInput, 'Please enter a valid email address.');
                isValid = false;
            }

            // Password Validation
            const passwordValue = passwordInput.value;
            if (!passwordValue) {
                showError(passwordInput, 'Password is required.');
                isValid = false;
            } else if (passwordValue.length < 6) {
                showError(passwordInput, 'Password must be at least 6 characters long.');
                isValid = false;
            }

            // Confirm Password Validation
            const confirmPasswordValue = confirmPasswordInput.value;
            if (!confirmPasswordValue) {
                showError(confirmPasswordInput, 'Please confirm your password.');
                isValid = false;
            } else if (passwordValue !== confirmPasswordValue) {
                showError(confirmPasswordInput, 'Passwords do not match.');
                isValid = false;
            }

            if (!isValid) {
                event.preventDefault();
            }
        });
    }

    // Login Form Validation
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        const emailInput = document.getElementById('email');
        const passwordInput = document.getElementById('password');

        loginForm.addEventListener('submit', (event) => {
            let isValid = true;
            clearAllErrors(loginForm);

            // Email Validation
            const emailValue = emailInput.value.trim();
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailValue) {
                showError(emailInput, 'Email address is required.');
                isValid = false;
            } else if (!emailRegex.test(emailValue)) {
                showError(emailInput, 'Please enter a valid email address.');
                isValid = false;
            }

            // Password Validation
            if (!passwordInput.value) {
                showError(passwordInput, 'Password is required.');
                isValid = false;
            }

            if (!isValid) {
                event.preventDefault();
            }
        });
    }

    // Helper functions
    function showError(inputElement, message) {
        inputElement.classList.add('input-error');
        const hint = inputElement.parentElement.querySelector('.error-hint');
        if (hint) {
            hint.textContent = message;
            hint.classList.add('visible');
        }
    }

    function clearAllErrors(form) {
        const errorInputs = form.querySelectorAll('.input-error');
        errorInputs.forEach(input => input.classList.remove('input-error'));

        const hints = form.querySelectorAll('.error-hint');
        hints.forEach(hint => {
            hint.textContent = '';
            hint.classList.remove('visible');
        });
    }
});
