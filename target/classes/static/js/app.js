let allocationPieChart, currentChart, targetChart;
let globalAiInsights = null;

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
                el.className = 'negative';
                el.style.color = 'var(--red)';
            }
        }
    });

    // Update Performance Metrics
    if (data.currentValue !== undefined) {
        document.getElementById('performanceMetrics').style.display = 'flex';
        document.getElementById('investedAmount').textContent = '₹' + data.investedAmount.toLocaleString();
        document.getElementById('currentValue').textContent = '₹' + data.currentValue.toLocaleString();
        
        const gainVal = document.getElementById('gainVal');
        const gainPercent = document.getElementById('gainPercent');
        
        gainVal.textContent = (data.gain >= 0 ? '+₹' : '-₹') + Math.abs(data.gain).toLocaleString();
        gainPercent.textContent = (data.gainPercent >= 0 ? '+' : '') + data.gainPercent.toFixed(2) + '%';
        
        gainVal.className = 'perf-val ' + (data.gain >= 0 ? 'text-green' : 'text-red');
        gainPercent.className = 'perf-badge ' + (data.gain >= 0 ? 'positive' : 'negative');
    }

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
                <div class="reb-legend-item">
                    <span class="reb-legend-dot" style="background: ${colors[i % colors.length]};"></span>
                    <span class="reb-legend-label">${a.assetType}</span>
                    <span class="reb-legend-value">${a.allocationPercent}%</span>
                </div>
            `).join('');
        }
    }

    // Update Stats Grid
    if (data.statsGrid) {
        if (data.statsGrid.equities) document.getElementById('statEquities').textContent = data.statsGrid.equities;
        if (data.statsGrid.commodities) document.getElementById('statCommodities').textContent = data.statsGrid.commodities;
        if (data.statsGrid.debt) document.getElementById('statDebt').textContent = data.statsGrid.debt;
        if (data.statsGrid.fixedIncome) document.getElementById('statFixedIncome').textContent = data.statsGrid.fixedIncome;
    }

    // Update client name badge
    if (data.clientName) {
        const recBadge = document.getElementById('recClientName');
        if (recBadge) recBadge.textContent = data.clientName;
    }

    // Populate Recommendations Table
    if (data.recommendations && data.recommendations.length > 0) {
        const tbody = document.getElementById('recommendationsTableBody');
        if (tbody) {
            tbody.innerHTML = data.recommendations.map(rec => `
                <tr>
                    <td>
                        <span class="table-action ${rec.action.toLowerCase()}">
                            <i data-lucide="${rec.action === 'Buy' ? 'trending-up' : rec.action === 'Sell' ? 'trending-down' : 'minus'}" style="width: 14px; height: 14px;"></i>
                            ${rec.action}
                        </span>
                    </td>
                    <td><strong>${rec.stock}</strong></td>
                    <td>${rec.current}</td>
                    <td>${rec.target}</td>
                    <td>${rec.units}</td>
                    <td>${rec.amount}</td>
                </tr>
            `).join('');
            if (window.lucide) lucide.createIcons();
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

    // Update Scores Summary
    const scoresBar = document.getElementById('portfolioScores');
    if (data.portfolioScores && scoresBar) {
        scoresBar.style.display = 'flex';
        document.getElementById('beforeOverlap').textContent = data.portfolioScores.before.overlap + '%';
        document.getElementById('afterOverlap').textContent = data.portfolioScores.after.overlap + '%';
        document.getElementById('beforeDiversification').textContent = data.portfolioScores.before.diversification;
        document.getElementById('afterDiversification').textContent = data.portfolioScores.after.diversification;
    }

    // Render Optimized Plan
    if (data.optimizedActions) {
        const planCard = document.getElementById('optimizationPlanCard');
        const actionsList = document.getElementById('optimizationActions');
        if (planCard && actionsList) {
            planCard.style.display = 'block';
            actionsList.innerHTML = data.optimizedActions.map(action => `
                <div class="reb-opt-item">
                    <div class="reb-opt-icon ${action.action}">
                        <i data-lucide="${action.action === 'increase' ? 'trending-up' : 'trending-down'}" style="width: 16px; height: 16px;"></i>
                    </div>
                    <div class="reb-opt-content">
                        <div>
                            <span class="reb-opt-name">${action.name}</span>
                            <span class="reb-opt-change ${action.action === 'increase' ? 'positive' : 'negative'}">
                                ${action.change}
                            </span>
                        </div>
                        <div class="reb-opt-reason">${action.reason}</div>
                    </div>
                </div>
            `).join('');
        }
    }

    // Populate Flags
    const flagsCard = document.getElementById('flagsCard');
    const flagsList = document.getElementById('flagsList');
    if (data.flags && data.flags.length > 0 && flagsCard && flagsList) {
        flagsCard.style.display = 'block';
        flagsList.innerHTML = data.flags.map(f => `<div>${f}</div>`).join('');
    } else if (flagsCard) {
        flagsCard.style.display = 'none';
    }

    if (data.agentTrace) {
        renderAgentTrace(data.agentTrace);
    }


    // Update Matrix
    if (data.matrix && data.matrix.length > 0) {
        renderMatrix('matrixHeaderRow', 'matrixTableBody', data.matrix);
    }

    // Update AI Audio Insights
    if (data.aiInsights) {
        globalAiInsights = data.aiInsights;
        document.getElementById('aiAudioCard').style.display = 'block';
    }

}

function renderMatrix(headerId, bodyId, matrixData) {
    const getCellClass = (val) => {
        if (val === 100) return 'gray';
        if (val === 0 || val === '-') return 'gray';
        if (val > 35) return 'red';
        if (val >= 20) return 'orange';
        if (val >= 10) return 'yellow';
        return 'green';
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
            <td style="color: #94a3b8; font-size: 0.75rem;">${i + 1}</td>
            <td style="text-align: left; min-width: 200px; font-size: 0.8rem; font-weight: 500;">${row.fund}</td>
            ${row.overlaps.map((val) => `
                <td>
                    <div class="matrix-cell ${getCellClass(val)}">${val}</div>
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
    const language = document.getElementById('languageSelectTop').value;

    console.log(`[Frontend] Analyzing portfolio for Client: ${clientId}, Language: ${language}`);

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
            body: JSON.stringify({ clientId, language })
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
            btn.textContent = 'Analyze & Generate Voice';
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

    trace.forEach((step, index) => {
        setTimeout(() => {
            const stepEl = document.createElement('div');
            stepEl.className = `trace-step ${step.type}`;
            stepEl.style.animationDelay = `0ms`;
            stepEl.innerHTML = `
                <div class="trace-step-content">
                    <div class="trace-step-title">${step.title}</div>
                    <div class="trace-step-detail">${step.detail}</div>
                </div>
            `;
            stepsEl.appendChild(stepEl);
        }, index * 200);
    });
}

function playAudio(mode) {
    if (!globalAiInsights) {
        console.warn('No AI insights available yet.');
        return;
    }

    const language = document.getElementById('languageSelectTop').value;
    console.log('[Audio] Current Insights:', globalAiInsights);
    console.log('[Audio] Requested Mode:', mode, 'with language:', language);

    let modeWithLang = mode;
    if (language === 'Tamil') modeWithLang += '_ta';
    else if (language === 'Hindi') modeWithLang += '_hin';

    const text = globalAiInsights[modeWithLang] || globalAiInsights[mode];
    if (!text) {
        console.warn('No AI insight available for mode:', modeWithLang);
        return;
    }

    // Cancel any ongoing speech
    window.speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(text);
    
    // Attempt to find a suitable voice based on language
    const voices = window.speechSynthesis.getVoices();
    
    if (language === 'Tamil') {
        utterance.lang = 'ta-IN';
        // Try to find a Tamil voice, fallback to any Indian voice
        utterance.voice = voices.find(v => v.lang === 'ta-IN') || voices.find(v => v.lang.startsWith('ta')) || voices.find(v => v.lang.includes('India'));
    } else if (language === 'Hindi') {
        utterance.lang = 'hi-IN';
        utterance.voice = voices.find(v => v.lang === 'hi-IN') || voices.find(v => v.lang.startsWith('hi')) || voices.find(v => v.lang.includes('India'));
    } else {
        utterance.lang = 'en-US';
        utterance.voice = voices.find(v => v.lang.startsWith('en')) || voices[0];
    }

    utterance.rate = 0.9;
    utterance.pitch = 1.0;
    
    // Some browsers need a tiny delay to reset properly
    setTimeout(() => {
        window.speechSynthesis.speak(utterance);
    }, 50);
}

async function shareViaEmail() {
    const email = document.getElementById('shareEmailInput').value;
    const clientId = document.getElementById('clientSelect').value;
    const language = document.getElementById('languageSelectTop').value;
    const mode = document.getElementById('shareEmailMode').value;
    const btn = document.getElementById('shareEmailBtn');
    const status = document.getElementById('shareEmailStatus');

    if (!email || !email.includes('@')) {
        alert('Please enter a valid email address.');
        return;
    }

    btn.disabled = true;
    btn.textContent = 'Sending...';
    status.style.display = 'block';
    status.style.color = 'var(--text-muted)';
    status.textContent = 'Preparing consolidated report...';

    try {
        const response = await fetch('/api/portfolio/share-email', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ clientId, recipientEmail: email, language, mode })
        });

        const data = await response.json();
        if (data.status === 'SUCCESS') {
            status.style.color = '#10b981';
            status.textContent = '✅ ' + data.message;
            document.getElementById('shareEmailInput').value = '';
        } else {
            status.style.color = '#ef4444';
            status.textContent = '❌ ' + data.message;
        }
    } catch (e) {
        status.style.color = '#ef4444';
        status.textContent = '❌ Error: ' + e.message;
    } finally {
        btn.disabled = false;
        btn.textContent = 'Send Report';
    }
}

function stopAudio() {
    window.speechSynthesis.cancel();
}

// Global expose
window.analyzePortfolio = analyzePortfolio;
window.playAudio = playAudio;
window.stopAudio = stopAudio;
window.shareViaEmail = shareViaEmail;
