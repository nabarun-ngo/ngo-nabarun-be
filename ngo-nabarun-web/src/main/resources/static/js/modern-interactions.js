/**
 * Modern NGO Website Interactions
 * Enhanced JavaScript for better user experience
 */

document.addEventListener('DOMContentLoaded', function() {
    
    // Initialize all interactive components
    initNavbarScrollEffect();
    initBackToTopButton();
    initDonationAmountButtons();
    initAnimationOnScroll();
    initFormEnhancements();
    initSmoothScrolling();
    initTeamInteractions();
    initAdvancedAnimations();
    
    /**
     * Navbar scroll effect
     */
    function initNavbarScrollEffect() {
        const navbar = document.querySelector('.fixed-top .navbar');
        if (!navbar) return;
        
        window.addEventListener('scroll', function() {
            if (window.scrollY > 100) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }
        });
    }
    
    /**
     * Back to top button
     */
    function initBackToTopButton() {
        const backToTopBtn = document.getElementById('backToTop');
        if (!backToTopBtn) return;
        
        window.addEventListener('scroll', function() {
            if (window.scrollY > 500) {
                backToTopBtn.classList.add('show');
            } else {
                backToTopBtn.classList.remove('show');
            }
        });
        
        backToTopBtn.addEventListener('click', function(e) {
            e.preventDefault();
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }
    
    /**
     * Donation amount buttons
     */
    function initDonationAmountButtons() {
        const amountButtons = document.querySelectorAll('.amount-btn');
        const amountInput = document.getElementById('amount');
        
        if (!amountButtons.length || !amountInput) return;
        
        amountButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                
                // Remove active class from all buttons
                amountButtons.forEach(btn => btn.classList.remove('active'));
                
                // Add active class to clicked button
                this.classList.add('active');
                
                // Set amount if not custom
                if (!this.classList.contains('custom-amount')) {
                    const amount = this.dataset.amount;
                    amountInput.value = amount;
                    amountInput.focus();
                } else {
                    amountInput.value = '';
                    amountInput.focus();
                }
            });
        });
        
        // Handle manual input
        amountInput.addEventListener('input', function() {
            amountButtons.forEach(btn => {
                if (btn.dataset.amount === this.value) {
                    btn.classList.add('active');
                } else {
                    btn.classList.remove('active');
                }
            });
        });
    }
    
    /**
     * Animation on scroll
     */
    function initAnimationOnScroll() {
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };
        
        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animate-in');
                }
            });
        }, observerOptions);
        
        // Observe elements with animation classes
        const animatedElements = document.querySelectorAll('.service-card, .testimonial-card, .impact-stat-card, .mission-point, .stat-card, .modern-team-card');
        animatedElements.forEach(el => {
            observer.observe(el);
        });
    }
    
    /**
     * Form enhancements
     */
    function initFormEnhancements() {
        // Modern input focus effects
        const modernInputs = document.querySelectorAll('.modern-input');
        modernInputs.forEach(input => {
            input.addEventListener('focus', function() {
                this.parentElement.classList.add('focused');
            });
            
            input.addEventListener('blur', function() {
                this.parentElement.classList.remove('focused');
                if (this.value) {
                    this.parentElement.classList.add('has-value');
                } else {
                    this.parentElement.classList.remove('has-value');
                }
            });
        });
        
        // Newsletter subscription
        const newsletterBtn = document.querySelector('.newsletter-btn');
        const newsletterInput = document.querySelector('.newsletter-input');
        
        if (newsletterBtn && newsletterInput) {
            newsletterBtn.addEventListener('click', function() {
                const email = newsletterInput.value.trim();
                if (email && isValidEmail(email)) {
                    showNotification('Thank you for subscribing to our newsletter!', 'success');
                    newsletterInput.value = '';
                } else {
                    showNotification('Please enter a valid email address.', 'error');
                }
            });
        }
    }
    
    /**
     * Smooth scrolling for anchor links
     */
    function initSmoothScrolling() {
        const anchorLinks = document.querySelectorAll('a[href^="#"]');
        anchorLinks.forEach(link => {
            link.addEventListener('click', function(e) {
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    e.preventDefault();
                    const targetOffset = target.offsetTop - 80; // Account for fixed header
                    window.scrollTo({
                        top: targetOffset,
                        behavior: 'smooth'
                    });
                }
            });
        });
    }
    
    /**
     * Team interactions
     */
    function initTeamInteractions() {
        // Team member contact buttons
        const contactBtns = document.querySelectorAll('.contact-member-btn');
        contactBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const memberName = this.closest('.modern-team-card').querySelector('.member-name').textContent;
                showNotification(`We'll connect you with ${memberName} soon!`, 'success');
            });
        });
        
        // Social media links hover effects
        const socialBtns = document.querySelectorAll('.social-btn');
        socialBtns.forEach(btn => {
            btn.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.1) rotate(5deg)';
            });
            
            btn.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1) rotate(0deg)';
            });
        });
        
        // Team card parallax effect
        const teamCards = document.querySelectorAll('.modern-team-card');
        teamCards.forEach(card => {
            card.addEventListener('mousemove', function(e) {
                const rect = this.getBoundingClientRect();
                const x = e.clientX - rect.left;
                const y = e.clientY - rect.top;
                
                const xPercent = (x / rect.width - 0.5) * 20;
                const yPercent = (y / rect.height - 0.5) * 20;
                
                const img = this.querySelector('.team-profile-img');
                if (img) {
                    img.style.transform = `scale(1.08) translate(${xPercent * 0.5}px, ${yPercent * 0.5}px)`;
                }
            });
            
            card.addEventListener('mouseleave', function() {
                const img = this.querySelector('.team-profile-img');
                if (img) {
                    img.style.transform = 'scale(1.08) translate(0px, 0px)';
                }
            });
        });
    }
    
    /**
     * Advanced animations
     */
    function initAdvancedAnimations() {
        // Staggered animation for team cards
        const teamCards = document.querySelectorAll('.modern-team-card');
        const teamObserver = new IntersectionObserver(function(entries) {
            entries.forEach((entry, index) => {
                if (entry.isIntersecting) {
                    setTimeout(() => {
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateY(0)';
                    }, index * 150);
                    teamObserver.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        });
        
        teamCards.forEach((card, index) => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(50px)';
            card.style.transition = 'all 0.6s cubic-bezier(0.16, 1, 0.3, 1)';
            teamObserver.observe(card);
        });
        
        // Floating animation for status badges
        const statusBadges = document.querySelectorAll('.team-status-badge');
        statusBadges.forEach(badge => {
            setInterval(() => {
                badge.style.animation = 'float 3s ease-in-out infinite';
            }, Math.random() * 2000 + 1000);
        });
        
        // Counter animation for impact stats
        initCounterAnimations();
    }
    
    /**
     * Counter animations for statistics
     */
    function initCounterAnimations() {
        const counters = document.querySelectorAll('.stat-content h3, .impact-stat-card h3');
        const counterObserver = new IntersectionObserver(function(entries) {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const target = entry.target;
                    const text = target.textContent;
                    const number = parseInt(text.replace(/[^0-9]/g, ''));
                    
                    if (number && number > 0) {
                        animateCounter(target, 0, number, 2000);
                        counterObserver.unobserve(target);
                    }
                }
            });
        }, {
            threshold: 0.5
        });
        
        counters.forEach(counter => {
            counterObserver.observe(counter);
        });
    }
    
    /**
     * Animate counter from start to end
     */
    function animateCounter(element, start, end, duration) {
        const startTime = performance.now();
        const originalText = element.textContent;
        const suffix = originalText.replace(/[0-9,]/g, '');
        
        function updateCounter(currentTime) {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            const current = Math.floor(start + (end - start) * easeOutQuart(progress));
            element.textContent = current.toLocaleString() + suffix;
            
            if (progress < 1) {
                requestAnimationFrame(updateCounter);
            }
        }
        
        requestAnimationFrame(updateCounter);
    }
    
    /**
     * Easing function for smooth animations
     */
    function easeOutQuart(t) {
        return 1 - (--t) * t * t * t;
    }
    
    /**
     * Utility functions
     */
    function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }
    
    function showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
    
        // Create content container
        const contentDiv = document.createElement('div');
        contentDiv.className = 'notification-content';
    
        // Create and append icon
        const icon = document.createElement('i');
        icon.className = `fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'} me-2`;
        contentDiv.appendChild(icon);
    
        // Safely append text content
        contentDiv.appendChild(document.createTextNode(message));
    
        // Append content to notification
        notification.appendChild(contentDiv);
    
        // Add to page
        document.body.appendChild(notification);
        
        // Show notification
        setTimeout(() => notification.classList.add('show'), 100);
        
        // Remove after 5 seconds
        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => document.body.removeChild(notification), 300);
        }, 5000);
    }
    
    /**
     * Card hover effects
     */
    const cards = document.querySelectorAll('.service-card, .about-image-card, .donation-form-card, .testimonial-card');
    cards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
    
    /**
     * Progress bar animations for impact stats
     */
    function animateProgressBars() {
        const progressBars = document.querySelectorAll('.progress-bar');
        progressBars.forEach(bar => {
            const width = bar.getAttribute('data-width') || bar.style.width;
            bar.style.width = '0%';
            setTimeout(() => {
                bar.style.width = width;
            }, 500);
        });
    }
    
    // Initialize progress bars when they come into view
    const progressObserver = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                animateProgressBars();
                progressObserver.unobserve(entry.target);
            }
        });
    });
    
    const progressSection = document.querySelector('.about-image-card');
    if (progressSection) {
        progressObserver.observe(progressSection);
    }
});

// Add notification styles
const notificationStyles = `
<style>
.notification {
    position: fixed;
    top: 20px;
    right: 20px;
    background: white;
    border-radius: 10px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.15);
    padding: 15px 20px;
    transform: translateX(400px);
    transition: transform 0.3s ease;
    z-index: 10000;
    max-width: 300px;
}

.notification.show {
    transform: translateX(0);
}

.notification-success {
    border-left: 4px solid #27ae60;
}

.notification-error {
    border-left: 4px solid #e74c3c;
}

.notification-content {
    display: flex;
    align-items: center;
    font-weight: 500;
    color: #2c3e50;
}

.animate-in {
    animation: slideInUp 0.6s ease forwards;
}

@keyframes slideInUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.modern-input-group.focused .form-label {
    color: #e74c3c;
}

.modern-input-group.has-value .form-label {
    font-weight: 600;
}

@keyframes float {
    0%, 100% {
        transform: translateY(0px) scale(1);
    }
    50% {
        transform: translateY(-10px) scale(1.05);
    }
}

.modern-team-card {
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.social-btn {
    transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.team-profile-img {
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
</style>
`;

document.head.insertAdjacentHTML('beforeend', notificationStyles);