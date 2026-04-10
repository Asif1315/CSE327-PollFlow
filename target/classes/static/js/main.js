document.addEventListener('DOMContentLoaded', function() {
    initAnimations();
    initFormValidation();
    initPolls();
});

function initAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-fadeIn');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);

    document.querySelectorAll('.stat-card, .feature-card, .category-card, .action-card').forEach(el => {
        observer.observe(el);
    });
}

function initFormValidation() {
    const forms = document.querySelectorAll('form');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            let valid = true;
            const inputs = form.querySelectorAll('input[required], textarea[required]');
            
            inputs.forEach(input => {
                if (!input.value.trim()) {
                    valid = false;
                    input.parentElement.classList.add('error');
                } else {
                    input.parentElement.classList.remove('error');
                }
            });
            
            if (!valid) {
                e.preventDefault();
                showToast('Please fill in all required fields', 'error');
            }
        });
    });
}

function initPolls() {
    document.querySelectorAll('.poll-vote-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const pollId = this.dataset.pollId;
            const optionId = this.dataset.optionId;
            vote(pollId, optionId);
        });
    });

    document.querySelectorAll('.poll-favorite-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const pollId = this.dataset.pollId;
            toggleFavorite(pollId, this);
        });
    });
}

function vote(pollId, optionId) {
    fetch(`/api/polls/${pollId}/vote`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ optionId: optionId })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('Vote submitted successfully!', 'success');
            updatePollResults(pollId);
        } else {
            showToast(data.message || 'Failed to submit vote', 'error');
        }
    })
    .catch(error => {
        showToast('An error occurred. Please try again.', 'error');
    });
}

function toggleFavorite(pollId, btn) {
    fetch(`/api/polls/${pollId}/favorite`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            btn.classList.toggle('active');
            const icon = btn.querySelector('i');
            icon.classList.toggle('bi-heart');
            icon.classList.toggle('bi-heart-fill');
            showToast(data.message, 'success');
        }
    })
    .catch(error => {
        showToast('An error occurred', 'error');
    });
}

function updatePollResults(pollId) {
    fetch(`/api/polls/${pollId}/results`)
        .then(response => response.json())
        .then(data => {
            const resultsContainer = document.querySelector(`#results-${pollId}`);
            if (resultsContainer && data.results) {
                resultsContainer.innerHTML = data.results.map(r => `
                    <div class="result-option">
                        <div class="result-label">${r.option}</div>
                        <div class="result-bar">
                            <div class="result-fill" style="width: ${r.percentage}%"></div>
                        </div>
                        <div class="result-percentage">${r.percentage}%</div>
                    </div>
                `).join('');
            }
        });
}

function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <i class="bi bi-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
        <span>${message}</span>
    `;
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.classList.add('show');
    }, 10);
    
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

document.querySelectorAll('.category-card').forEach(card => {
    card.addEventListener('mouseenter', function() {
        this.style.transform = 'translateY(-8px) scale(1.02)';
    });
    
    card.addEventListener('mouseleave', function() {
        this.style.transform = 'translateY(0) scale(1)';
    });
});

window.addEventListener('scroll', function() {
    const navbar = document.querySelector('.navbar');
    if (navbar) {
        if (window.scrollY > 50) {
            navbar.style.background = 'rgba(255, 255, 255, 0.95)';
            navbar.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.1)';
        } else {
            navbar.style.background = 'rgba(255, 255, 255, 0.8)';
            navbar.style.boxShadow = 'none';
        }
    }
});

const style = document.createElement('style');
style.textContent = `
    .toast {
        position: fixed;
        bottom: 2rem;
        right: 2rem;
        display: flex;
        align-items: center;
        gap: 0.75rem;
        padding: 1rem 1.5rem;
        background: var(--white);
        border-radius: var(--radius);
        box-shadow: var(--shadow-xl);
        transform: translateY(100px);
        opacity: 0;
        transition: all 0.3s ease;
        z-index: 9999;
        font-weight: 500;
    }
    
    .toast.show {
        transform: translateY(0);
        opacity: 1;
    }
    
    .toast-success { border-left: 4px solid var(--success); }
    .toast-success i { color: var(--success); }
    
    .toast-error { border-left: 4px solid var(--danger); }
    .toast-error i { color: var(--danger); }
    
    .toast-info { border-left: 4px solid var(--primary); }
    .toast-info i { color: var(--primary); }
    
    .input-group.error input {
        border-color: var(--danger);
    }
    
    .input-group.error::after {
        content: 'This field is required';
        position: absolute;
        bottom: -1.25rem;
        left: 0;
        font-size: 0.75rem;
        color: var(--danger);
    }
`;
document.head.appendChild(style);