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
    if (data.matrix && data.matrix.length > 0) {
        const getBgStyle = (val) => {
            if (val === 100) return 'background-color: #2563eb; color: white; font-weight: bold;'; // Dark blue for 100%
            if (val === 0) return 'background-color: white; color: transparent;'; // Hide upper triangle
            if (val >= 60) return 'background-color: #3b82f6; color: white;';
            if (val >= 40) return 'background-color: #60a5fa; color: white;';
            if (val >= 20) return 'background-color: #93c5fd; color: #1e3a8a;';
            if (val >= 10) return 'background-color: #bfdbfe; color: #1e3a8a;';
            return 'background-color: #dbeafe; color: #1e3a8a;'; // Very light blue
        };

        const headerRow = document.getElementById('matrixHeaderRow');
        const matrixBody = document.getElementById('matrixTableBody');
        
        // 1. Build Headers: Sr, Investment Name, 1, 2, 3...
        let headerHtml = `<th>Sr</th><th>Investment Name</th>`;
        for (let i = 1; i <= data.matrix.length; i++) {
            headerHtml += `<th style="text-align: center; width: 40px;">${i}</th>`;
        }
        headerRow.innerHTML = headerHtml;

        // 2. Build Body
        matrixBody.innerHTML = data.matrix.map((row, i) => `
            <tr>
                <td style="color: var(--text-muted); font-size: 0.8rem;">${i + 1}</td>
                <td style="min-width: 250px;">
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                        <div class="icon-wrapper" style="background-color: ${row.iconColor}; width: 24px; height: 24px;">
                            <i data-lucide="activity" style="width: 12px; height: 12px;"></i>
                        </div>
                        <span style="font-size: 0.85rem;">${row.fund}</span>
                    </div>
                </td>
                ${row.overlaps.map((val, j) => `
                    <td style="text-align: center; padding: 0.5rem; border: 1px solid #f1f5f9; font-size: 0.8rem; ${getBgStyle(val)}">
                        ${val > 0 ? val : ''}
                    </td>
                `).join('')}
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

function renderAgentTrace(trace) {
    const card = document.getElementById('agentTraceCard');
    const stepsEl = document.getElementById('agentTraceSteps');
    if (!trace || !trace.length || !card || !stepsEl) return;

    card.style.display = 'block';
    stepsEl.innerHTML = '';

    // Render each trace step with a staggered delay so it looks "live"
    trace.forEach((step, index) => {
        setTimeout(() => {
            const div = document.createElement('div');
            div.className = `trace-step ${step.type}`;
            div.style.animationDelay = `0ms`; // already staggered by setTimeout
            div.innerHTML = `
                <div class="trace-step-content">
                    <div class="trace-step-title">${step.title}</div>
                    <div class="trace-step-detail">${step.detail}</div>
                </div>
            `;
            stepsEl.appendChild(div);
        }, index * 200); // 200ms stagger between each step
    });

    if (window.lucide) lucide.createIcons();
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
