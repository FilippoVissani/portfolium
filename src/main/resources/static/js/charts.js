// Chart.js initialization and configuration for Portfolium dashboard
// This file expects portfolioData to be available in the global scope

function initializeCharts(portfolioData) {
    // Chart.js default configuration for professional look
    Chart.defaults.font.family = 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
    Chart.defaults.color = '#64748b';
    Chart.defaults.borderColor = '#e2e8f0';

    // Section 1: Main Bank Account
    if (portfolioData.liquidity.statistics) {
        initMainBankMonthlyTrendChart(portfolioData);
        initMainBankExpenseCategoryChart(portfolioData);
    }

    // Section 2: Planned Expenses
    initPlannedExpensesCoverageChart(portfolioData);
    if (portfolioData.planned.isInvested && portfolioData.planned.historicalPerformance) {
        initHistoricalChart('plannedExpensesHistoricalChart', portfolioData.planned.historicalPerformance);
    }

    // Section 3: Emergency Fund
    initEmergencyFundProgressChart(portfolioData);
    if (!portfolioData.emergency.isLiquid && portfolioData.emergency.historicalPerformance) {
        initHistoricalChart('emergencyFundHistoricalChart', portfolioData.emergency.historicalPerformance);
    }

    // Section 4: Investments
    if (portfolioData.investments.itemsWithWeights && portfolioData.investments.itemsWithWeights.length > 0) {
        initInvestmentsBreakdownChart(portfolioData);
    }
    if (portfolioData.historicalPerformance) {
        initHistoricalChart('investmentsHistoricalChart', portfolioData.historicalPerformance);
    }

    // Section 5: Overall Performance
    initOverallAssetAllocationChart(portfolioData);
    initOverallNetWorthChart(portfolioData);
    if (portfolioData.overallHistoricalPerformance) {
        initHistoricalChart('overallHistoricalPerformanceChart', portfolioData.overallHistoricalPerformance);
    }
}

// Section 1: Main Bank Account Charts
function initMainBankMonthlyTrendChart(portfolioData) {
    const ctx = document.getElementById('mainBankMonthlyTrendChart');
    if (!ctx) return;

    const stats = portfolioData.liquidity.statistics;
    const labels = stats.monthlyTrend.map(m => m.yearMonth);
    const incomeData = stats.monthlyTrend.map(m => parseFloat(m.income));
    const expenseData = stats.monthlyTrend.map(m => parseFloat(m.expense));

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Income',
                    data: incomeData,
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4
                },
                {
                    label: 'Expense',
                    data: expenseData,
                    borderColor: '#ef4444',
                    backgroundColor: 'rgba(239, 68, 68, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4
                }
            ]
        },
        options: getStandardLineChartOptions()
    });
}

function initMainBankExpenseCategoryChart(portfolioData) {
    const ctx = document.getElementById('mainBankExpenseCategoryChart');
    if (!ctx) return;

    const stats = portfolioData.liquidity.statistics;
    const labels = stats.topExpenseCategories.map(c => c[0]);
    const values = stats.topExpenseCategories.map(c => parseFloat(c[1]));
    const colors = ["#ef4444", "#f59e0b", "#eab308", "#84cc16", "#22c55e"];

    new Chart(ctx, {
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
        options: getStandardPieChartOptions()
    });
}

// Section 2: Planned Expenses Charts
function initPlannedExpensesCoverageChart(portfolioData) {
    const ctx = document.getElementById('plannedExpensesCoverageChart');
    if (!ctx) return;

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Estimated', 'Accrued'],
            datasets: [{
                label: 'Amount (€)',
                data: [
                    parseFloat(portfolioData.planned.totalEstimated),
                    parseFloat(portfolioData.planned.totalAccrued)
                ],
                backgroundColor: ['#7c3aed', '#10b981'],
                borderRadius: 8,
                borderWidth: 0
            }]
        },
        options: getStandardBarChartOptions()
    });
}

// Section 3: Emergency Fund Charts
function initEmergencyFundProgressChart(portfolioData) {
    const ctx = document.getElementById('emergencyFundProgressChart');
    if (!ctx) return;

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Target', 'Current'],
            datasets: [{
                label: 'Amount (€)',
                data: [
                    parseFloat(portfolioData.emergency.targetCapital),
                    parseFloat(portfolioData.emergency.currentCapital)
                ],
                backgroundColor: ['#f59e0b', '#10b981'],
                borderRadius: 8,
                borderWidth: 0
            }]
        },
        options: getStandardBarChartOptions()
    });
}

// Section 4: Investments Charts
function initInvestmentsBreakdownChart(portfolioData) {
    const ctx = document.getElementById('investmentsBreakdownChart');
    if (!ctx) return;

    const labels = portfolioData.investments.itemsWithWeights.map(item => item.ticker);
    const values = portfolioData.investments.itemsWithWeights.map(item => parseFloat(item.currentValue));
    const colors = ["#2563eb", "#7c3aed", "#ec4899", "#f59e0b", "#10b981", "#06b6d4"];

    new Chart(ctx, {
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
        options: getStandardPieChartOptions()
    });
}

// Section 5: Overall Performance Charts
function initOverallAssetAllocationChart(portfolioData) {
    const ctx = document.getElementById('overallAssetAllocationChart');
    if (!ctx) return;

    new Chart(ctx, {
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
        options: getStandardPieChartOptions()
    });
}

function initOverallNetWorthChart(portfolioData) {
    const ctx = document.getElementById('overallNetWorthChart');
    if (!ctx) return;

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Emergency Fund', 'Investments', 'Liquidity'],
            datasets: [{
                label: 'Value (€)',
                data: [
                    parseFloat(portfolioData.emergency.currentCapital),
                    parseFloat(portfolioData.investments.totalCurrent),
                    parseFloat(portfolioData.liquidity.net)
                ],
                backgroundColor: ['#f59e0b', '#10b981', '#06b6d4'],
                borderRadius: 8,
                borderWidth: 0
            }]
        },
        options: getStandardBarChartOptions()
    });
}

// Generic Historical Performance Chart
function initHistoricalChart(canvasId, historicalData) {
    const ctx = document.getElementById(canvasId);
    if (!ctx || !historicalData || !historicalData.dataPoints) return;

    const labels = historicalData.dataPoints.map(dp => dp.date);
    const values = historicalData.dataPoints.map(dp => parseFloat(dp.value));

    // Determine if overall trend is positive or negative for color
    const firstValue = values[0] || 0;
    const lastValue = values[values.length - 1] || 0;
    const isPositive = lastValue >= firstValue;
    const lineColor = isPositive ? '#10b981' : '#ef4444';
    const gradientColor = isPositive ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)';

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Portfolio Value (€)',
                data: values,
                borderColor: lineColor,
                backgroundColor: gradientColor,
                borderWidth: 3,
                fill: true,
                tension: 0.4,
                pointRadius: 3,
                pointHoverRadius: 5,
                pointBackgroundColor: lineColor,
                pointBorderColor: '#fff',
                pointBorderWidth: 2
            }]
        },
        options: getStandardLineChartOptions()
    });
}

// Standard chart options
function getStandardLineChartOptions() {
    return {
        responsive: true,
        maintainAspectRatio: true,
        interaction: {
            mode: 'index',
            intersect: false
        },
        plugins: {
            legend: {
                display: true,
                position: 'top',
                labels: {
                    padding: 15,
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
                        return context.dataset.label + ': €' + context.parsed.y.toFixed(2);
                    }
                }
            }
        },
        scales: {
            y: {
                beginAtZero: false,
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
                    font: { size: 11 },
                    maxRotation: 45,
                    minRotation: 45
                }
            }
        }
    };
}

function getStandardBarChartOptions() {
    return {
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
    };
}

function getStandardPieChartOptions() {
    return {
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
                        const value = context.parsed;
                        const total = context.dataset.data.reduce((a, b) => a + b, 0);
                        const percentage = ((value / total) * 100).toFixed(2);
                        return context.label + ': €' + value.toFixed(2) + ' (' + percentage + '%)';
                    }
                }
            }
        }
    };
}

