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

    // Historical Performance Chart (if data available)
    if (portfolioData.historicalPerformance && portfolioData.historicalPerformance.dataPoints) {
        initHistoricalPerformanceChart(portfolioData);
    }
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

function initHistoricalPerformanceChart(portfolioData) {
    const perfCtx = document.getElementById('historicalPerformanceChart');
    if (!perfCtx) return;

    const historicalData = portfolioData.historicalPerformance;
    const allDataPoints = historicalData.dataPoints;

    // Store the chart instance globally so we can update it
    let performanceChart = null;

    // Function to downsample data points for better visualization
    function downsampleData(data, maxPoints) {
        if (data.length <= maxPoints) {
            return data;
        }

        const result = [];
        const interval = Math.ceil(data.length / maxPoints);

        // Always include the first point
        result.push(data[0]);

        // Sample points at intervals
        for (let i = interval; i < data.length - 1; i += interval) {
            result.push(data[i]);
        }

        // Always include the last point
        result.push(data[data.length - 1]);

        return result;
    }

    // Function to filter data based on selected period
    function filterDataByPeriod(period) {
        const today = new Date();
        let startDate = null;
        let maxPoints = null;

        if (period === 'ALL') {
            // For ALL, limit to ~100 points for better performance
            maxPoints = 100;
            return downsampleData(allDataPoints, maxPoints);
        } else if (period === '1M') {
            startDate = new Date(today);
            startDate.setMonth(startDate.getMonth() - 1);
        } else if (period === '6M') {
            startDate = new Date(today);
            startDate.setMonth(startDate.getMonth() - 6);
        } else if (period === 'YTD') {
            startDate = new Date(today.getFullYear(), 0, 1);
        } else if (period === '5Y') {
            startDate = new Date(today);
            startDate.setFullYear(startDate.getFullYear() - 5);
            // For 5Y, limit to ~150 points
            maxPoints = 150;
        }

        if (!startDate) return allDataPoints;

        const filtered = allDataPoints.filter(dp => {
            const dpDate = new Date(dp.date);
            return dpDate >= startDate;
        });

        // Apply downsampling if maxPoints is set
        return maxPoints ? downsampleData(filtered, maxPoints) : filtered;
    }

    // Function to calculate return percentage
    function calculateReturn(data) {
        if (data.length < 2) return 0;
        const firstValue = parseFloat(data[0].value);
        const lastValue = parseFloat(data[data.length - 1].value);
        return (((lastValue - firstValue) / firstValue) * 100).toFixed(2);
    }

    // Function to update chart with filtered data
    function updateChart(period) {
        const filteredData = filterDataByPeriod(period);
        const labels = filteredData.map(dp => dp.date);
        const values = filteredData.map(dp => parseFloat(dp.value));

        // Adjust point size based on number of data points
        const pointRadius = filteredData.length > 100 ? 2 : filteredData.length > 50 ? 3 : 4;
        const pointHoverRadius = pointRadius + 2;

        // Calculate return for this period
        const periodReturn = calculateReturn(filteredData);

        // Update return badge
        const returnBadge = document.getElementById('returnBadge');
        if (returnBadge) {
            returnBadge.textContent = periodReturn + '%';
            returnBadge.className = 'return-badge ' + (periodReturn >= 0 ? 'positive' : 'negative');
        }

        // Determine if overall trend is positive or negative for color
        const firstValue = values[0] || 0;
        const lastValue = values[values.length - 1] || 0;
        const isPositive = lastValue >= firstValue;
        const lineColor = isPositive ? '#10b981' : '#ef4444';
        const gradientColor = isPositive
            ? 'rgba(16, 185, 129, 0.1)'
            : 'rgba(239, 68, 68, 0.1)';

        if (performanceChart) {
            // Update existing chart
            performanceChart.data.labels = labels;
            performanceChart.data.datasets[0].data = values;
            performanceChart.data.datasets[0].borderColor = lineColor;
            performanceChart.data.datasets[0].backgroundColor = gradientColor;
            performanceChart.data.datasets[0].pointBackgroundColor = lineColor;
            performanceChart.data.datasets[0].pointRadius = pointRadius;
            performanceChart.data.datasets[0].pointHoverRadius = pointHoverRadius;
            performanceChart.update('none'); // Update without animation
        } else {
            // Create new chart
            performanceChart = new Chart(perfCtx, {
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
                        pointRadius: pointRadius,
                        pointHoverRadius: pointHoverRadius,
                        pointBackgroundColor: lineColor,
                        pointBorderColor: '#fff',
                        pointBorderWidth: 2
                    }]
                },
                options: {
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
                                    return 'Value: €' + context.parsed.y.toFixed(2);
                                },
                                afterLabel: function(context) {
                                    if (context.dataIndex > 0) {
                                        const prev = values[context.dataIndex - 1];
                                        const current = values[context.dataIndex];
                                        const change = ((current - prev) / prev * 100).toFixed(2);
                                        return 'Change: ' + (change >= 0 ? '+' : '') + change + '%';
                                    }
                                    return '';
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
                }
            });
        }
    }

    // Initialize with ALL period
    updateChart('ALL');

    // Add event listeners to period buttons
    const periodButtons = document.querySelectorAll('.period-btn');
    periodButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Remove active class from all buttons
            periodButtons.forEach(btn => btn.classList.remove('active'));
            // Add active class to clicked button
            this.classList.add('active');
            // Update chart with selected period
            updateChart(this.getAttribute('data-period'));
        });
    });
}
