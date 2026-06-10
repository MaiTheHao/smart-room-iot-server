import { getAverageHistory } from '../../../../api/temperature.api.js';
import { StateManager } from '../../state_manager.js';

let chartInstance = null;

export const RoomTempChart = {
  init() {
    const tempEl = document.querySelector('#tempChart');
    if (!tempEl || !window.ApexCharts) return;

    const i18n = StateManager.getI18n();
    const options = {
      chart: {
        height: 300,
        type: 'area',
        toolbar: { show: false },
        fontFamily: 'inherit',
        animations: { enabled: true },
      },
      dataLabels: { enabled: false },
      stroke: { curve: 'smooth', width: 3 },
      xaxis: {
        type: 'datetime',
        labels: { style: { colors: '#94a3b8', fontSize: '11px' } },
      },
      grid: { borderColor: '#f1f5f9', strokeDashArray: 4 },
      tooltip: { x: { format: 'dd MMM HH:mm' } },
      yaxis: {
        labels: {
          formatter: (val) => (val?.toFixed ? val.toFixed(2) : val),
          style: { colors: '#94a3b8', fontSize: '11px' },
        },
      },
      fill: {
        type: 'gradient',
        gradient: { shadeIntensity: 1, opacityFrom: 0.4, opacityTo: 0.05, stops: [0, 90, 100] },
      },
      series: [{ name: i18n.temp || 'Temperature', data: [] }],
      colors: ['#ef4444'],
      markers: {
        size: 4,
        strokeColors: '#fff',
        strokeWidth: 2,
        hover: {
          size: 6
        }
      },
    };

    chartInstance = new ApexCharts(tempEl, options);
    chartInstance.render();
  },

  async update(from, to) {
    const el = document.querySelector('#tempChart');
    if (!el || !chartInstance) return;

    const roomId = StateManager.getRoomId();
    const i18n = StateManager.getI18n();

    const [err, res] = await getAverageHistory(roomId, from, to);
    if (err || !res.data) return;

    const data = res.data;
    if (data.length > 0) {
      const avg = (data.reduce((sum, item) => sum + item.avgTempC, 0) / data.length).toFixed(2);
      const badge = document.querySelector('#avgTempBadge');
      if (badge) badge.textContent = `Avg: ${avg}°C`;

      chartInstance.updateOptions({
        series: [
          {
            name: i18n.temp || 'Temperature',
            data: data.map((item) => ({ x: new Date(item.timestamp).getTime(), y: item.avgTempC })),
          },
        ],
      });
    }
  },
};
