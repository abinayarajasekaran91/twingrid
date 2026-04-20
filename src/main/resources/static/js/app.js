let allocationPieChart, currentChart, targetChart;

document.addEventListener('DOMContentLoaded', () => {
    initCharts();
    
    // Automatically trigger analysis for the first selected user
    analyzePortfolio();

    // Trigger analysis when a new client is selected
    document.getElementById('clientSelect').addEventListener('change', analyzePortfolio);
    document.getElementById('analyzeBtn').addEventListener('click', analyzePortfolio);
});

function initCharts() {

    // Current Allocation Pie Chart (by Asset Type)
    const pieCtx = document.getElementById('allocationPieChart').getContext('2d');
    allocationPieChart = new Chart(pieCtx, {
        type: 'doughnut',
        data: {
            labels: [],
            datasets: [{
                data: [],
                backgroundColor: ['#8b5cf6', '#3b82f6', '#f59e0b', '#10b981', '#ef4444', '#06b6d4'],
                borderWidth: 0,
                cutout: '70%'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            }
        }
    });

    // Rebalancing Summary Current Chart
    const currentCtx = document.getElementById('currentChart').getContext('2d');
    currentChart = new Chart(currentCtx, {
        type: 'doughnut',
        data: {
            datasets: [{
                data: [85, 10, 5],
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
        let val = data.totalRebalanceCost;
        if (val === '₹0.00' || val === '$0.00' || val === '0' || val === '0.00') {
            el.textContent = '-';
            el.className = 'negative';
            el.style.color = 'var(--text-muted)';
        } else {
            el.textContent = val;
            if(val && val.includes('+')) {
                el.className = 'negative positive text-green';
                el.style.color = 'var(--green)';
            } else {
                el.className = 'negative text-red';
                el.style.color = 'var(--red)';
            }
        }
    });



    // Update Charts Data
    if (data.assetTypeAllocations) {
        const labels = data.assetTypeAllocations.map(a => a.assetType);
        const values = data.assetTypeAllocations.map(a => a.allocationPercent);
        
        allocationPieChart.data.labels = labels;
        allocationPieChart.data.datasets[0].data = values;
        allocationPieChart.update();

        // Update Custom Legend
        const pieLegend = document.getElementById('pieLegend');
        if (pieLegend) {
            const colors = allocationPieChart.data.datasets[0].backgroundColor;
            pieLegend.innerHTML = data.assetTypeAllocations.map((a, i) => `
                <div style="display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem;">
                    <span style="width: 12px; height: 12px; border-radius: 50%; background: ${colors[i % colors.length]};"></span>
                    <span style="font-weight: 600; color: var(--text-main);">${a.assetType}</span>
                    <span style="color: var(--text-muted);">${a.allocationPercent}%</span>
                </div>
            `).join('');
        }
    }

    if (data.currentAllocations) {
        currentChart.data.datasets[0].data = data.currentAllocations;
        currentChart.update();
    }
    if (data.targetAllocations) {
        targetChart.data.datasets[0].data = data.targetAllocations;
        targetChart.update();
    }


    // Update Flags (NEW)
    if (data.flags && data.flags.length > 0) {
        const flagsCard = document.getElementById('flagsCard');
        const flagsList = document.getElementById('flagsList');
        if (flagsCard && flagsList) {
            flagsCard.style.display = 'block';
            flagsList.innerHTML = data.flags.map(f => `<div>${f}</div>`).join('');
        }
    }

    // Update Portfolio Scores (NEW)
    if (data.portfolioScores) {
        const scoresEl = document.getElementById('portfolioScores');
        if (scoresEl) {
            scoresEl.style.display = 'flex';
            document.getElementById('beforeOverlap').textContent = data.portfolioScores.before.overlapScore + '%';
            document.getElementById('afterOverlap').textContent = data.portfolioScores.after.overlapScore + '%';
            document.getElementById('beforeDiversification').textContent = data.portfolioScores.before.diversificationScore;
            document.getElementById('afterDiversification').textContent = data.portfolioScores.after.diversificationScore;
        }
    }

    // Update Optimized Actions (NEW)
    if (data.optimizedActions && data.optimizedActions.length > 0) {
        const optCard = document.getElementById('optimizationPlanCard');
        const optList = document.getElementById('optimizationActions');
        if (optCard && optList) {
            optCard.style.display = 'block';
            optList.innerHTML = data.optimizedActions.map(action => `
                <div class="opt-action-item">
                    <div class="opt-action-header">
                        <span class="opt-fund-name">${action.fund}</span>
                        <span class="badge-${action.action === 'INCREASE' ? 'green' : 'red'}">${action.action}</span>
                        <span class="opt-change-text">${action.from}% <i data-lucide="arrow-right" style="width:12px;"></i> ${action.to}%</span>
                    </div>
                    <div class="opt-action-reason">${action.reason}</div>
                </div>
            `).join('');
        }
    }

    // Update Performance Metrics (NEW)
    if (data.currentValue !== undefined) {
        const perfEl = document.getElementById('performanceMetrics');
        if (perfEl) {
            perfEl.style.display = 'flex';
            document.getElementById('investedAmount').textContent = formatCurrency(data.investedAmount);
            document.getElementById('currentValue').textContent = formatCurrency(data.currentValue);
            document.getElementById('gainVal').textContent = formatCurrency(data.gain);
            
            const gpEl = document.getElementById('gainPercent');
            gpEl.textContent = (data.gainPercent >= 0 ? '+' : '') + data.gainPercent.toFixed(2) + '%';
            gpEl.className = 'perf-badge ' + (data.gainPercent >= 0 ? 'positive' : 'negative');
        }
    }

    // Update Agent Trace
    if (data.agentTrace) {
        renderAgentTrace(data.agentTrace);
    }


    // Update Matrix
    if (data.matrix && data.matrix.length > 0) {
        renderMatrix('matrixHeaderRow', 'matrixTableBody', data.matrix);
    }

}

function renderMatrix(headerId, bodyId, matrixData) {
    const getBgStyle = (val) => {
        if (val === 100) return 'background-color: #1e3a8a; color: white; font-weight: bold;'; 
        if (val === 0 || val === '-') return 'background-color: white; color: #94a3b8;'; 
        if (val > 35) return 'background-color: #ef4444; color: white;'; // Red 🚨
        if (val >= 20) return 'background-color: #f97316; color: white;'; // Orange ⚠️
        if (val >= 10) return 'background-color: #f59e0b; color: white;'; // Yellow 🟡
        return 'background-color: #10b981; color: white;'; // Green 🟢
    };

    const headerRow = document.getElementById(headerId);
    const matrixBody = document.getElementById(bodyId);
    
    if (!headerRow || !matrixBody) return;

    let headerHtml = `<th>Sr</th><th>Investment Name</th>`;
    for (let i = 1; i <= matrixData.length; i++) {
        headerHtml += `<th style="text-align: center; width: 40px;">${i}</th>`;
    }
    headerRow.innerHTML = headerHtml;

    matrixBody.innerHTML = matrixData.map((row, i) => `
        <tr>
            <td style="color: var(--text-muted); font-size: 0.8rem;">${i + 1}</td>
            <td style="min-width: 250px;">
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <div class="icon-wrapper" style="background-color: ${row.iconColor || '#3b82f6'}; width: 24px; height: 24px;">
                        <i data-lucide="activity" style="width: 12px; height: 12px;"></i>
                    </div>
                    <span style="font-size: 0.85rem;">${row.fund}</span>
                </div>
            </td>
            ${row.overlaps.map((val, j) => `
                <td style="text-align: center; padding: 0.5rem; border: 1px solid #f1f5f9; font-size: 0.8rem; ${getBgStyle(val)}">
                    ${val}
                </td>
            `).join('')}
        </tr>
    `).join('');


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

function formatCurrency(val) {
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        maximumFractionDigits: 0
    }).format(val);
}
