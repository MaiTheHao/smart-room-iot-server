import { METRIC_CONFIGS } from './metric_registry.js';
import { ChartFactory } from '../chart/chart_factory.js';
import { StateManager } from '../../state_manager.js';

export const RoomChartManager = (() => {
  const METRIC_ORDER = ['TEMPERATURE', 'ENERGY', 'HUMIDITY', 'CO2', 'LUX'];

  let activeIndex = 0;
  let currentPage = 1;
  let currentFrom = null;
  let currentTo = null;
  let isMobile = window.innerWidth < 768;
  let isEventsBound = false;

  const activeChartInstances = [];

  const getPageSize = () => (isMobile ? 1 : 4);

  const getTotalPages = () => Math.ceil(METRIC_ORDER.length / getPageSize());

  const getColClass = (indexOnPage, totalOnPage) => {
    if (isMobile) return 'col-12 mb-4';
    if (totalOnPage === 1 || (totalOnPage === 3 && indexOnPage === 2)) {
      return 'col-12 mb-4';
    }
    return 'col-lg-6 col-12 mb-4';
  };

  const renderMobileNavBar = () => {
    const navBar = document.querySelector('#mobileChartNavBar');
    if (!navBar) return;

    if (!isMobile) {
      navBar.classList.add('d-none');
      return;
    }

    navBar.classList.remove('d-none');
    const i18n = StateManager.getI18n() || {};

    navBar.innerHTML = METRIC_ORDER.map((metricId, idx) => {
      const config = METRIC_CONFIGS[metricId];
      if (!config) return '';

      const title = i18n[config.titleKey] || config.defaultTitle;
      const isActive = idx === activeIndex;

      const activeStyle = isActive
        ? `background-color: ${config.color}; color: #fff; border-color: ${config.color};`
        : `background-color: #f8fafc; color: #64748b; border-color: #e2e8f0;`;

      return `
        <button type="button" class="btn btn-sm rounded-pill px-3 py-2 fw-semibold btn-mobile-tab"
                data-index="${idx}"
                style="${activeStyle} transition: all 0.2s ease;">
          <i data-lucide="${config.icon}" class="lucide-sm me-1"></i>
          ${title}
        </button>
      `;
    }).join('');

    navBar.querySelectorAll('.btn-mobile-tab').forEach((btn) => {
      btn.onclick = (e) => {
        const targetBtn = e.currentTarget;
        const idx = parseInt(targetBtn.getAttribute('data-index'), 10);
        if (!isNaN(idx)) {
          activeIndex = idx;
          currentPage = activeIndex + 1;
          renderCurrentPage();
        }
      };
    });

    if (window.renderIcons) window.renderIcons();
  };

  const updatePaginationUI = () => {
    const totalPages = getTotalPages();
    const pagControls = document.querySelector('#chartPaginationControls');
    const indicator = document.querySelector('#chartPageIndicator');
    const btnPrev = document.querySelector('#btnPrevChartPage');
    const btnNext = document.querySelector('#btnNextChartPage');

    if (!pagControls) return;

    if (isMobile || totalPages <= 1) {
      pagControls.classList.add('d-none');
    } else {
      pagControls.classList.remove('d-none');
      if (indicator) indicator.textContent = `${currentPage} / ${totalPages}`;
      if (btnPrev) btnPrev.disabled = currentPage === 1;
      if (btnNext) btnNext.disabled = currentPage === totalPages;
    }
  };

  const destroyActiveCharts = () => {
    activeChartInstances.forEach((inst) => inst.destroy());
    activeChartInstances.length = 0;
  };

  const renderCurrentPage = () => {
    const container = document.querySelector('#roomChartsContainer');
    if (!container) return;

    destroyActiveCharts();
    container.innerHTML = '';

    const pageSize = getPageSize();
    const totalPages = getTotalPages();

    if (currentPage > totalPages) currentPage = 1;

    const startIndex = (currentPage - 1) * pageSize;
    const pageMetrics = METRIC_ORDER.slice(startIndex, startIndex + pageSize);
    const totalOnPage = pageMetrics.length;

    pageMetrics.forEach((metricId, idx) => {
      const config = METRIC_CONFIGS[metricId];
      if (!config) return;

      const colClass = getColClass(idx, totalOnPage);
      const cardCol = document.createElement('div');
      cardCol.className = colClass;

      const i18n = StateManager.getI18n() || {};
      const title = i18n[config.titleKey] || config.defaultTitle;

      cardCol.innerHTML = `
        <div class="card rounded-2 border-1 shadow-sm h-100">
          <div class="card-header bg-white border-0 py-3 d-flex justify-content-between align-items-center">
            <h6 class="m-0 flex-grow-1 d-flex align-items-center fw-bold">
              <i data-lucide="${config.icon}" class="me-2 lucide-sm" style="color: ${config.color}"></i>
              <span>${title}</span>
            </h6>
            <span class="badge bg-opacity-10 rounded-pill px-3 fw-semibold" id="badge-${config.id}" style="background-color: ${config.color}20; color: ${config.color}">--</span>
          </div>
          <div class="card-body p-2">
            <div id="chart-${config.id}" style="min-height: 280px"></div>
          </div>
        </div>
      `;

      container.appendChild(cardCol);

      const chartEl = cardCol.querySelector(`#chart-${config.id}`);
      const badgeEl = cardCol.querySelector(`#badge-${config.id}`);

      const instance = ChartFactory.createInstance(config, chartEl, {
        getI18n: () => StateManager.getI18n(),
        fetchData: async (from, to) => {
          const [err, res] = await config.fetchHistory(StateManager.getRoomId(), from, to);
          return [err, res?.data ?? null];
        },
        onData: (chart, data, { config: cfg }) => {
          if (badgeEl && cfg.badgeFormatter) {
            badgeEl.textContent = cfg.badgeFormatter(data, cfg.valueExtractor);
          }
        },
      });

      if (instance) {
        activeChartInstances.push(instance);
        if (currentFrom && currentTo) {
          instance.update(currentFrom, currentTo).catch((error) => {
            console.error('[RoomChartManager] Chart update failed:', error);
          });
        }
      }
    });

    renderMobileNavBar();
    updatePaginationUI();
    if (window.renderIcons) window.renderIcons();
  };

  const bindEvents = () => {
    if (isEventsBound) return;

    const btnPrev = document.querySelector('#btnPrevChartPage');
    const btnNext = document.querySelector('#btnNextChartPage');

    if (btnPrev) {
      btnPrev.onclick = () => {
        if (currentPage > 1) {
          currentPage--;
          renderCurrentPage();
        }
      };
    }
    if (btnNext) {
      btnNext.onclick = () => {
        if (currentPage < getTotalPages()) {
          currentPage++;
          renderCurrentPage();
        }
      };
    }

    let resizeTimer;
    window.addEventListener('resize', () => {
      clearTimeout(resizeTimer);
      resizeTimer = setTimeout(() => {
        const newIsMobile = window.innerWidth < 768;
        if (newIsMobile !== isMobile) {
          isMobile = newIsMobile;
          currentPage = 1;
          activeIndex = 0;
          renderCurrentPage();
        }
      }, 200);
    });

    isEventsBound = true;
  };

  return {
    init(from, to) {
      currentFrom = from;
      currentTo = to;
      currentPage = 1;
      activeIndex = 0;
      isMobile = window.innerWidth < 768;
      bindEvents();
      renderCurrentPage();
    },
    updateAll(from, to) {
      currentFrom = from;
      currentTo = to;
      activeChartInstances.forEach((inst) =>
        inst.update(from, to).catch((error) => {
          console.error('[RoomChartManager] Chart update failed:', error);
        })
      );
    },
    destroy() {
      destroyActiveCharts();
    },
  };
})();
