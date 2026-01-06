// Chart.js initialization and configuration for Portfolium dashboard
// This file expects portfolioData to be available in the global scope

function initializeCharts(portfolioData) {
    // Chart.js default configuration for professional look
    Chart.defaults.font.family = 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
    Chart.defaults.color = '#64748b';
    Chart.defaults.borderColor = '#e2e8f0';

    // Asset Allocation Chart
    initAssetAllocationChart(portfolioData);

    // Net Worth Distribution Chart
    initNetWorthChart(portfolioData);

    // Investments Breakdown Chart
    if (portfolioData.investments.itemsWithWeights && portfolioData.investments.itemsWithWeights.length > 0) {
        initInvestmentsChart(portfolioData);
    }

    // Planned Expenses Chart
    initPlannedExpensesChart(portfolioData);
}

function initAssetAllocationChart(portfolioData) {
    const assetCtx = document.getElementById('assetAllocationChart');
    if (!assetCtx) return;

    new Chart(assetCtx, {
        type: 'doughnut',
        data: {
            labels: ['Liquid', 'Invested'],
            datasets: [{
                data: [
                    portfolioData.percentLiquid,
                    portfolioData.percentInvested
                ],
                backgroundColor: ['#06b6d4', '#10b981'],
                borderWidth: 3,
                borderColor: '#fff',
                hoverOffset: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 20,
                        font: { size: 13, weight: '500' },
                        usePointStyle: true,
                        pointStyle: 'circle'
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(15, 23, 42, 0.95)',
                    padding: 12,
                    titleFont: { size: 14, weight: '600' },
                    bodyFont: { size: 13 },
                    borderColor: '#e2e8f0',
                    borderWidth: 1,
                    callbacks: {
                        label: function(context) {
                            return context.label + ': ' + context.parsed.toFixed(2) + '%';
                        }
                    }
                }
            }
        }
    });
}

function initNetWorthChart(portfolioData) {
    const netWorthCtx = document.getElementById('netWorthChart');
    if (!netWorthCtx) return;

    new Chart(netWorthCtx, {
        type: 'bar',
        data: {
            labels: ['Emergency Fund', 'Investments', 'Liquidity'],
            datasets: [{
                label: 'Value (€)',
                data: [
                    portfolioData.emergency.currentCapital,
                    portfolioData.investments.totalCurrent,
                    portfolioData.liquidity.net
                ],
                backgroundColor: ['#f59e0b', '#10b981', '#06b6d4'],
                borderRadius: 8,
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: 'rgba(15, 23, 42, 0.95)',
                    padding: 12,
                    titleFont: { size: 14, weight: '600' },
                    bodyFont: { size: 13 },
                    borderColor: '#e2e8f0',
                    borderWidth: 1,
                    callbacks: {
                        label: function(context) {
                            return 'Value: €' + context.parsed.y.toFixed(2);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: '#f1f5f9',
                        drawBorder: false
                    },
                    ticks: {
                        font: { size: 12 },
                        callback: function(value) {
                            return '€' + value.toLocaleString();
                        }
                    }
                },
                x: {
                    grid: {
                        display: false,
                        drawBorder: false
                    },
                    ticks: {
                        font: { size: 12, weight: '500' }
                    }
                }
            }
        }
    });
}

function initInvestmentsChart(portfolioData) {
    const investmentsCtx = document.getElementById('investmentsChart');
    if (!investmentsCtx) return;

    const labels = portfolioData.investments.itemsWithWeights.map(item => item.ticker);
    const values = portfolioData.investments.itemsWithWeights.map(item => item.currentValue);
    const colors = ["#2563eb", "#7c3aed", "#ec4899", "#f59e0b", "#10b981", "#06b6d4"];

    new Chart(investmentsCtx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: colors.slice(0, labels.length),
                borderWidth: 3,
                borderColor: '#fff',
                hoverOffset: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 15,
                        font: { size: 12, weight: '500' },
                        usePointStyle: true,
                        pointStyle: 'circle'
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(15, 23, 42, 0.95)',
                    padding: 12,
                    titleFont: { size: 14, weight: '600' },
                    bodyFont: { size: 13 },
                    borderColor: '#e2e8f0',
                    borderWidth: 1,
                    callbacks: {
                        label: function(context) {
                            return context.label + ': €' + context.parsed.toFixed(2);
                        }
                    }
                }
            }
        }
    });
}

function initPlannedExpensesChart(portfolioData) {
    const plannedCtx = document.getElementById('plannedExpensesChart');
    if (!plannedCtx) return;

    new Chart(plannedCtx, {
        type: 'bar',
        data: {
            labels: ['Estimated', 'Accrued'],
            datasets: [{
                label: 'Amount (€)',
                data: [
                    portfolioData.planned.totalEstimated,
                    portfolioData.planned.totalAccrued
                ],
                backgroundColor: ['#7c3aed', '#10b981'],
                borderRadius: 8,
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: 'rgba(15, 23, 42, 0.95)',
                    padding: 12,
                    titleFont: { size: 14, weight: '600' },
                    bodyFont: { size: 13 },
                    borderColor: '#e2e8f0',
                    borderWidth: 1,
                    callbacks: {
                        label: function(context) {
                            return 'Amount: €' + context.parsed.y.toFixed(2);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: '#f1f5f9',
                        drawBorder: false
                    },
                    ticks: {
                        font: { size: 12 },
                        callback: function(value) {
                            return '€' + value.toLocaleString();
                        }
                    }
                },
                x: {
                    grid: {
                        display: false,
                        drawBorder: false
                    },
                    ticks: {
                        font: { size: 12, weight: '500' }
                    }
                }
            }
        }
    });
}

