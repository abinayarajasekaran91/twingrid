let alignChart, currentChart, targetChart;

document.addEventListener('DOMContentLoaded', () => {
    initCharts();
    
    // Automatically trigger analysis for the first selected user
    analyzePortfolio();

    // Trigger analysis when a new client is selected
    document.getElementById('clientSelect').addEventListener('change', analyzePortfolio);
    document.getElementById('analyzeBtn').addEventListener('click', analyzePortfolio);
});

function initCharts() {
    // 5-Finger Strategy Alignment Chart
    const alignCtx = document.getElementById('alignmentChart').getContext('2d');
    alignChart = new Chart(alignCtx, {
        type: 'doughnut',
        data: {
            labels: ['Equities', 'Commodities', 'Debt'],
            datasets: [{
                data: [85, 10, 5],
                backgroundColor: ['#8b5cf6', '#3b82f6', '#f59e0b'],
                borderWidth: 0,
                cutout: '70%'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: { enabled: false }
            }
        }
    });

    // Rebalancing Summary Current Chart
    const currentCtx = document.getElementById('currentChart').getContext('2d');
    currentChart = new Chart(currentCtx, {
        type: 'doughnut',
        data: {
            datasets: [{
                data: [86, 10, 5],
                backgroundColor: ['#8b5cf6', '#3b82f6', '#f59e0b'],
                borderWidth: 0,
                cutout: '80%'
            }]
        },
        options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false }, tooltip: { enabled: false } } }
    });

    // Rebalancing Summary Target Chart
    const targetCtx = document.getElementById('targetChart').getContext('2d');
    targetChart = new Chart(targetCtx, {
        type: 'doughnut',
        data: {
            datasets: [{
                data: [70, 30, 0],
                backgroundColor: ['#0ea5e9', '#10b981', '#f3f4f6'],
                borderWidth: 0,
                cutout: '80%'
            }]
        },
        options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false }, tooltip: { enabled: false } } }
    });
}

function updateUI(data) {
    // Update Client Names
    document.querySelectorAll('.user-chip span').forEach(el => {
        if (!el.classList.contains('badge-purple')) {
            el.textContent = data.clientName;
        }
    });
    
    // Update Recommendations Intro Text
    const recText = document.querySelector('.recommendations-text p');
    if(recText) {
        recText.innerHTML = `We've generated a list of trades to rebalance <strong>${data.clientName}'s</strong> portfolio to better align with the 5-finger strategy. The cart is ready for review.`;
    }

    // Update Risk Level
    const riskBadge = document.querySelector('.badge-warning');
    if (riskBadge) {
        riskBadge.textContent = data.riskLevel;
    }
    
    // Update Total Cost everywhere
    document.querySelectorAll('.total-cost .negative').forEach(el => {
        el.textContent = data.totalRebalanceCost;
        if(data.totalRebalanceCost && data.totalRebalanceCost.includes('+')) {
            el.className = 'negative positive text-green'; // making it green for positive
            el.style.color = 'var(--green)';
        } else if (data.totalRebalanceCost === '$0.00') {
            el.className = 'negative';
            el.style.color = 'var(--text-muted)';
        } else {
            el.className = 'negative text-red';
            el.style.color = 'var(--red)';
        }
    });

    // Update Charts Data
    if (data.currentAllocations) {
        alignChart.data.datasets[0].data = data.currentAllocations;
        alignChart.update();
        
        currentChart.data.datasets[0].data = data.currentAllocations;
        currentChart.update();
    }
    if (data.targetAllocations) {
        targetChart.data.datasets[0].data = data.targetAllocations;
        targetChart.update();
    }

    // Update Cart
    if (data.recommendations) {
        const cartBody = document.getElementById('cartTableBody');
        cartBody.innerHTML = data.recommendations.map(item => `
            <tr>
                <td class="stock-cell">
                    <div class="stock-icon" style="background-color: ${item.color}">${item.name.charAt(0)}</div>
                    <div class="stock-info">
                        <strong>${item.name}</strong>
                        <span>0% <span class="${item.change.startsWith('+') ? 'positive text-green' : 'text-red'}">${item.change}</span></span>
                    </div>
                </td>
                <td class="reason-cell">${item.reason}</td>
                <td class="current-cell">${item.current}</td>
                <td>
                    <button class="btn-outline ${item.action === 'remove' ? 'remove' : ''}" ${item.action === 'none' ? 'disabled' : ''} onclick="toggleCartItem(this, '${item.action}')">
                        <i data-lucide="${item.action === 'remove' ? 'minus' : (item.action === 'none' ? 'check' : 'plus')}"></i> 
                        <span>${item.action === 'remove' ? 'Remove Cart' : (item.action === 'none' ? 'No Action' : 'Add to Cart')}</span>
                    </button>
                </td>
            </tr>
        `).join('');
    }

    // Update Matrix
    if (data.matrix) {
        const getBgClass = (val) => {
            if (val >= 80) return 'bg-purple-dark';
            if (val >= 40) return 'bg-purple-medium';
            if (val >= 20) return 'bg-purple-light';
            return 'bg-blue-light';
        };

        const matrixBody = document.getElementById('matrixTableBody');
        matrixBody.innerHTML = data.matrix.map(row => `
            <tr>
                <td>
                    <div class="icon-wrapper" style="background-color: ${row.iconColor}">
                        <i data-lucide="activity" style="width: 14px; height: 14px;"></i>
                    </div>
                    ${row.fund}
                </td>
                <td class="${getBgClass(row.m)}">${row.m}%</td>
                <td class="${getBgClass(row.r)}">${row.r}%</td>
                <td class="${getBgClass(row.mt)}">${row.mt}%</td>
            </tr>
        `).join('');
    }

    // Re-initialize lucide icons for dynamic elements
    if (window.lucide) {
        lucide.createIcons();
    }
}

async function analyzePortfolio() {
    const btn = document.getElementById('analyzeBtn');
    const clientId = document.getElementById('clientSelect').value;
    
    if(btn) {
        btn.textContent = 'Analyzing...';
        btn.disabled = true;
    }

    try {
        const response = await fetch('/api/portfolio/analyze', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ clientId })
        });
        
        if (response.ok) {
            const data = await response.json();
            updateUI(data);
        } else {
            console.error('Failed to fetch analysis', response.statusText);
        }
    } catch (e) {
        console.error('Error fetching analysis:', e);
    } finally {
        if(btn) {
            btn.textContent = 'Analyze Portfolio';
            btn.disabled = false;
        }
    }
}

function toggleCartItem(btn, action) {
    if (action === 'none') return;
    
    const span = btn.querySelector('span');
    const icon = btn.querySelector('i');
    
    if (btn.classList.contains('active')) {
        // Revert
        btn.classList.remove('active');
        btn.style.backgroundColor = 'transparent';
        btn.style.color = action === 'remove' ? 'var(--red)' : 'var(--purple)';
        span.textContent = action === 'remove' ? 'Remove Cart' : 'Add to Cart';
        icon.setAttribute('data-lucide', action === 'remove' ? 'minus' : 'plus');
    } else {
        // Apply
        btn.classList.add('active');
        btn.style.backgroundColor = action === 'remove' ? 'var(--red)' : 'var(--purple)';
        btn.style.color = 'white';
        span.textContent = action === 'remove' ? 'Removed' : 'Added';
        icon.setAttribute('data-lucide', 'check');
    }
    
    if (window.lucide) {
        lucide.createIcons();
    }
}
