/* ========================================
   Doctor Sign-Up Form - Frontend Validation
   ======================================== */

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('doctorSignupForm');
    const passwordField = document.getElementById('password');
    const confirmPasswordField = document.getElementById('confirmPassword');
    const togglePasswordBtns = document.querySelectorAll('.toggle-password');

    // Form submission
    form.addEventListener('submit', handleFormSubmit);

    // Real-time password validation
    passwordField.addEventListener('input', validatePassword);
    confirmPasswordField.addEventListener('input', validateConfirmPassword);

    // Toggle password visibility
    togglePasswordBtns.forEach(btn => {
        btn.addEventListener('click', togglePasswordVisibility);
    });
});

/* ========================================
   Form Submission Handler
   ======================================== */

function handleFormSubmit(e) {
    e.preventDefault();
    
    // Clear all previous error messages
    clearAllErrors();

    // Validate form
    if (validateForm()) {
        showSuccessMessage();
        // Reset form
        document.getElementById('doctorSignupForm').reset();
        clearAllErrors();
        
        // Simulate redirect after 2 seconds
        setTimeout(() => {
            // In a real application, this would submit to backend
            // window.location.href = 'doctor_login.html';
            alert('Form validated successfully! In production, this would be sent to the backend.');
            document.getElementById('successMessage').style.display = 'none';
        }, 2000);
    }
}

/* ========================================
   Main Validation Function
   ======================================== */

function validateForm() {
    let isValid = true;

    // Validate Full Name
    if (!validateFullName()) {
        isValid = false;
    }

    // Validate Email
    if (!validateEmail()) {
        isValid = false;
    }

    // Validate Phone
    if (!validatePhone()) {
        isValid = false;
    }

    // Validate License Number
    if (!validateLicenseNumber()) {
        isValid = false;
    }

    // Validate Specialization
    if (!validateSpecialization()) {
        isValid = false;
    }

    // Validate Password
    if (!validatePassword()) {
        isValid = false;
    }

    // Validate Confirm Password
    if (!validateConfirmPassword()) {
        isValid = false;
    }

    // Validate Terms & Conditions
    if (!validateTerms()) {
        isValid = false;
    }

    return isValid;
}

/* ========================================
   Individual Field Validation Functions
   ======================================== */

function validateFullName() {
    const field = document.getElementById('fullName');
    const errorElement = document.getElementById('fullNameError');
    const value = field.value.trim();

    if (value === '') {
        errorElement.textContent = 'Full name is required';
        field.classList.add('error');
        return false;
    }

    if (value.length < 3) {
        errorElement.textContent = 'Full name must be at least 3 characters';
        field.classList.add('error');
        return false;
    }

    if (!/^[a-zA-Z\s'-]+$/.test(value)) {
        errorElement.textContent = 'Full name can only contain letters, spaces, hyphens, and apostrophes';
        field.classList.add('error');
        return false;
    }

    field.classList.remove('error');
    field.classList.add('success');
    return true;
}

function validateEmail() {
    const field = document.getElementById('email');
    const errorElement = document.getElementById('emailError');
    const value = field.value.trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (value === '') {
        errorElement.textContent = 'Email address is required';
        field.classList.add('error');
        return false;
    }

    if (!emailRegex.test(value)) {
        errorElement.textContent = 'Please enter a valid email address';
        field.classList.add('error');
        return false;
    }

    field.classList.remove('error');
    field.classList.add('success');
    return true;
}

function validatePhone() {
    const field = document.getElementById('phone');
    const errorElement = document.getElementById('phoneError');
    const value = field.value.trim();
    const phoneRegex = /^[0-9+\-\s()]{10,}$/;

    if (value === '') {
        errorElement.textContent = 'Phone number is required';
        field.classList.add('error');
        return false;
    }

    if (!phoneRegex.test(value)) {
        errorElement.textContent = 'Please enter a valid phone number (minimum 10 digits)';
        field.classList.add('error');
        return false;
    }

    field.classList.remove('error');
    field.classList.add('success');
    return true;
}

function validateLicenseNumber() {
    const field = document.getElementById('licenseNumber');
    const errorElement = document.getElementById('licenseNumberError');
    const value = field.value.trim();

    if (value === '') {
        errorElement.textContent = 'Medical license number is required';
        field.classList.add('error');
        return false;
    }

    if (value.length < 5) {
        errorElement.textContent = 'Medical license number must be at least 5 characters';
        field.classList.add('error');
        return false;
    }

    if (!/^[a-zA-Z0-9]+$/.test(value)) {
        errorElement.textContent = 'Medical license number can only contain letters and numbers';
        field.classList.add('error');
        return false;
    }

    field.classList.remove('error');
    field.classList.add('success');
    return true;
}

function validateSpecialization() {
    const field = document.getElementById('specialization');
    const errorElement = document.getElementById('specializationError');
    const value = field.value;

    if (value === '') {
        errorElement.textContent = 'Please select a specialization';
        field.classList.add('error');
        return false;
    }

    field.classList.remove('error');
    field.classList.add('success');
    return true;
}

function validatePassword() {
    const field = document.getElementById('password');
    const errorElement = document.getElementById('passwordError');
    const value = field.value;
    const requirements = {
        length: value.length >= 8,
        uppercase: /[A-Z]/.test(value),
        lowercase: /[a-z]/.test(value),
        number: /[0-9]/.test(value)
    };

    // Show password requirements
    const requirementsDiv = document.querySelector('.password-requirements');
    requirementsDiv.style.display = 'block';

    // Update requirement indicators
    updateRequirementIndicator('req-length', requirements.length);
    updateRequirementIndicator('req-uppercase', requirements.uppercase);
    updateRequirementIndicator('req-lowercase', requirements.lowercase);
    updateRequirementIndicator('req-number', requirements.number);

    if (value === '') {
        errorElement.textContent = 'Password is required';
        field.classList.add('error');
        return false;
    }

    const allRequirementsMet = Object.values(requirements).every(req => req === true);

    if (!allRequirementsMet) {
        errorElement.textContent = 'Password does not meet all requirements';
        field.classList.add('error');
        return false;
    }

    field.classList.remove('error');
    field.classList.add('success');
    return true;
}

function validateConfirmPassword() {
    const passwordField = document.getElementById('password');
    const confirmField = document.getElementById('confirmPassword');
    const errorElement = document.getElementById('confirmPasswordError');
    const password = passwordField.value;
    const confirmPassword = confirmField.value;

    if (confirmPassword === '') {
        errorElement.textContent = 'Please confirm your password';
        confirmField.classList.add('error');
        return false;
    }

    if (password !== confirmPassword) {
        errorElement.textContent = 'Passwords do not match';
        confirmField.classList.add('error');
        return false;
    }

    confirmField.classList.remove('error');
    confirmField.classList.add('success');
    return true;
}

function validateTerms() {
    const field = document.getElementById('terms');
    const errorElement = document.getElementById('termsError');

    if (!field.checked) {
        errorElement.textContent = 'You must agree to the terms and conditions';
        return false;
    }

    return true;
}

/* ========================================
   Helper Functions
   ======================================== */

function updateRequirementIndicator(elementId, isMet) {
    const element = document.getElementById(elementId);
    if (isMet) {
        element.classList.add('met');
        element.textContent = '✓ ' + element.textContent.substring(2);
    } else {
        element.classList.remove('met');
        element.textContent = '✗ ' + element.textContent.substring(2);
    }
}

function togglePasswordVisibility(e) {
    e.preventDefault();
    
    // Determine which password field this button is for
    const inputField = this.previousElementSibling;
    const eyeIcon = this.querySelector('.eye-icon');
    
    if (inputField.type === 'password') {
        inputField.type = 'text';
        eyeIcon.textContent = '🙈';
    } else {
        inputField.type = 'password';
        eyeIcon.textContent = '👁️';
    }
}

function clearAllErrors() {
    const errorMessages = document.querySelectorAll('.error-message');
    const inputs = document.querySelectorAll('input, select');

    errorMessages.forEach(msg => {
        msg.textContent = '';
    });

    inputs.forEach(input => {
        input.classList.remove('error', 'success');
    });

    document.querySelector('.password-requirements').style.display = 'none';
}

function showSuccessMessage() {
    const successMsg = document.getElementById('successMessage');
    successMsg.style.display = 'block';
    successMsg.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

/* ========================================
   Real-time Validation
   ======================================== */

// Add real-time validation to all fields
document.getElementById('fullName').addEventListener('blur', validateFullName);
document.getElementById('email').addEventListener('blur', validateEmail);
document.getElementById('phone').addEventListener('blur', validatePhone);
document.getElementById('licenseNumber').addEventListener('blur', validateLicenseNumber);
document.getElementById('specialization').addEventListener('change', validateSpecialization);
document.getElementById('confirmPassword').addEventListener('blur', validateConfirmPassword);

// Validate email in real-time
document.getElementById('email').addEventListener('input', function() {
    const field = this;
    const value = field.value.trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    
    if (value && emailRegex.test(value)) {
        field.classList.remove('error');
        field.classList.add('success');
    } else if (value) {
        field.classList.add('error');
    }
});

// Validate phone in real-time
document.getElementById('phone').addEventListener('input', function() {
    const field = this;
    const value = field.value.trim();
    const phoneRegex = /^[0-9+\-\s()]{10,}$/;
    
    if (value && phoneRegex.test(value)) {
        field.classList.remove('error');
        field.classList.add('success');
    } else if (value) {
        field.classList.add('error');
    }
});

// Match password and confirm password in real-time
document.getElementById('password').addEventListener('input', function() {
    const confirmField = document.getElementById('confirmPassword');
    if (confirmField.value && this.value !== confirmField.value) {
        confirmField.classList.add('error');
    } else if (confirmField.value) {
        confirmField.classList.remove('error');
    }
});

document.getElementById('confirmPassword').addEventListener('input', function() {
    const passwordField = document.getElementById('password');
    if (this.value && passwordField.value !== this.value) {
        this.classList.add('error');
    } else if (this.value) {
        this.classList.remove('error');
    }
});
