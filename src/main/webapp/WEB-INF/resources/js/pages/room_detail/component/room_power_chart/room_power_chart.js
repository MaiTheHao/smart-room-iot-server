import { getEnergyMetricHistory } from '../../../../api/metric.api.js';
import { StateManager } from '../../state_manager.js';

let chartInstance = null;

export const RoomPowerChart = {
  init() {
    const powerEl = document.querySelector('#powerChart');
    if (!powerEl || !window.ApexCharts) return;

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
      series: [{ name: i18n.power || 'Power', data: [] }],
      colors: ['#f59e0b'],
      markers: {
        size: 4,
        strokeColors: '#fff',
        strokeWidth: 2,
        hover: {
          size: 6
        }
      },
    };

    chartInstance = new ApexCharts(powerEl, options);
    chartInstance.render();
  },

  async update(from, to) {
    const el = document.querySelector('#powerChart');
    if (!el || !chartInstance) return;

    const roomId = StateManager.getRoomId();
    const i18n = StateManager.getI18n();

    const [err, res] = await getEnergyMetricHistory({
      category: 'ROOM',
      targetId: roomId,
      from,
      to,
    });
    if (err || !res.data) return;

    const data = res.data;
    if (data.length > 0) {
      const peak = Math.max(...data.map((item) => item.power || 0)).toFixed(2);
      const badge = document.querySelector('#peakPowerBadge');
      if (badge) badge.textContent = `Peak: ${peak}W`;

      chartInstance.updateOptions({
        series: [
          {
            name: i18n.power || 'Power',
            data: data.map((item) => ({ x: new Date(item.timestamp).getTime(), y: item.power || 0 })),
          },
        ],
      });
    }
  },
};
