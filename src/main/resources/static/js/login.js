function switchTab(tabName) {
    // Update tabs
    document.querySelectorAll('.tab').forEach(tab => {
        tab.classList.remove('active');
        if (tab.textContent.toLowerCase().replace(' ', '') === tabName) {
            tab.classList.add('active');
        }
    });

    // Update forms
    document.querySelectorAll('.auth-form').forEach(form => {
        form.classList.remove('active');
    });
    
    if (tabName === 'login' || tabName === 'signin') {
        document.getElementById('loginForm').classList.add('active');
    } else {
        document.getElementById('registerForm').classList.add('active');
    }
}

document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('loginError');
    const btn = document.getElementById('loginBtn');
    
    errorDiv.textContent = '';
    btn.textContent = 'Signing in...';
    btn.disabled = true;

    try {
        const response = await fetch('/api/auth/signin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });
        
        if (response.ok) {
            const data = await response.json();
            // Assuming successful login, redirect to dashboard
            window.location.href = '/index.html';
        } else {
            const err = await response.json();
            errorDiv.textContent = err.message || 'Invalid credentials. Please try again.';
        }
    } catch (error) {
        console.error('Login error:', error);
        errorDiv.textContent = 'Network error. Please try again later.';
    } finally {
        btn.textContent = 'Sign In';
        btn.disabled = false;
    }
});

document.getElementById('registerForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const errorDiv = document.getElementById('registerError');
    // For hackathon purposes, registration isn't wired up to a backend endpoint yet
    errorDiv.textContent = 'Registration is not fully connected. Please use Sign In with test credentials.';
    errorDiv.style.color = 'var(--red)';
});
