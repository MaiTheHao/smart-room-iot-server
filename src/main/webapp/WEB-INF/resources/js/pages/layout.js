(function () {
  'use strict';
  const MENU_CONFIG = [
    {
      type: 'tree',
      label: /*[[#{menu.dashboard}]]*/ 'Dashboard',
      icon: 'layout-dashboard',
      visible: true,
      children: [
        {
          label: /*[[#{index.title}]]*/ 'Analytics',
          icon: 'bar-chart-2',
          link: '/',
          visible: true,
        },
      ],
    },
    {
      type: 'header',
      label: /*[[#{menu.management}]]*/ 'Management',
      visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_ROOM", "F_MANAGE_FLOOR", "F_MANAGE_CLIENT", "F_MANAGE_FUNCTION", "F_MANAGE_ROLE", "F_MANAGE_GROUP")')}]]*/ true,
    },
    {
      type: 'tree',
      label: /*[[#{menu.location}]]*/ 'Location',
      icon: 'map-pin',
      visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_ROOM", "F_MANAGE_FLOOR")')}]]*/ true,
      children: [
        {
          label: /*[[#{menu.rooms}]]*/ 'Rooms',
          icon: 'door-open',
          link: '/management/rooms',
          visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_ROOM")')}]]*/ true,
        },
        {
          label: /*[[#{menu.floors}]]*/ 'Floors',
          icon: 'layers',
          link: '/management/floors',
          visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_FLOOR")')}]]*/ true,
        },
      ],
    },
    {
      type: 'tree',
      label: /*[[#{menu.access_control}]]*/ 'Access Control',
      icon: 'shield-check',
      visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_CLIENT", "F_MANAGE_FUNCTION", "F_MANAGE_ROLE", "F_MANAGE_GROUP")')}]]*/ true,
      children: [
        {
          label: /*[[#{menu.clients}]]*/ 'Clients',
          icon: 'contact',
          link: '/management/clients',
          visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_CLIENT")')}]]*/ true,
        },
        {
          label: /*[[#{menu.functions}]]*/ 'Functions',
          icon: 'key',
          link: '/management/functions',
          visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_FUNCTION")')}]]*/ true,
        },
        {
          label: /*[[#{menu.roles}]]*/ 'Roles',
          icon: 'badge-check',
          link: '/management/roles',
          visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_ROLE")')}]]*/ true,
        },
        {
          label: /*[[#{menu.groups}]]*/ 'Groups',
          icon: 'users',
          link: '/management/groups',
          visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_GROUP")')}]]*/ true,
        },
      ],
    },
    {
      type: 'header',
      label: /*[[#{menu.smart_system}]]*/ 'Smart System',
      visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_AUTOMATION", "F_MANAGE_RULE")')}]]*/ true,
    },
    {
      label: /*[[#{menu.automations}]]*/ 'Automations',
      icon: 'zap',
      link: '/management/smart-system/automations',
      visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_AUTOMATION")')}]]*/ true,
    },
    {
      label: /*[[#{menu.rules}]]*/ 'Rules',
      icon: 'git-branch',
      link: '/management/smart-system/rules',
      visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_RULE")')}]]*/ true,
    },
    {
      type: 'header',
      label: /*[[#{menu.alert_system}]]*/ 'Alert System',
      visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_ALERT", "F_ACCESS_ALERT")')}]]*/ true,
    },
    {
      type: 'tree',
      label: /*[[#{menu.alerts}]]*/ 'Alerts',
      icon: 'bell',
      visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_ALERT", "F_ACCESS_ALERT")')}]]*/ true,
      children: [
        {
          label: /*[[#{menu.alert_manage}]]*/ 'Quản lý cấu hình',
          icon: 'settings-2',
          link: '/management/smart-system/alerts/manage',
          visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_ALERT")')}]]*/ true,
        },
        {
          label: /*[[#{menu.alert_list}]]*/ 'Xem sự kiện',
          icon: 'bell-ring',
          link: '/management/smart-system/alerts',
          visible: /*[[${#authorization.expression('hasAnyAuthority("F_MANAGE_ALL", "F_MANAGE_ALERT", "F_ACCESS_ALERT")')}]]*/ true,
        },
      ],
    },
  ];

  function Layout() {
    function renderMenu(items) {
      return items
        .filter((item) => item.visible !== false)
        .map((item) => {
          switch (item.type) {
            case 'header':
              return `<li class="nav-header fw-bold">${item.label}</li>`;
            case 'tree':
              return renderTree(item);
            default:
              return renderLink(item);
          }
        })
        .join('');
    }

    function renderLink(item, isChild = false) {
      return `
                <li class="nav-item">
                    <a href="${item.link || '#'}" class="nav-link ${isChild ? 'ps-4' : ''} d-flex align-items-center" data-link="${item.link || ''}">
                        <i class="nav-icon" data-lucide="${item.icon || 'circle'}"></i>
                        <p>${item.label}</p>
                    </a>
                </li>
            `;
    }

    function renderTree(item) {
      const visibleChildren = item.children ? item.children.filter((child) => child.visible !== false) : [];
      if (visibleChildren.length === 0) return '';

      return `
                <li class="nav-item">
                    <a href="#" class="nav-link d-flex align-items-center">
                        <i class="nav-icon" data-lucide="${item.icon || 'circle'}"></i>
                        <p>
                            ${item.label}
                            <i class="nav-arrow" data-lucide="chevron-right" style="width: 16px; height: 16px;"></i>
                        </p>
                    </a>
                    <ul class="nav nav-treeview">
                        ${visibleChildren.map((child) => renderLink(child, true)).join('')}
                    </ul>
                </li>
            `;
    }

    function initUserSession() {
      const isAuthenticated = /*[[${#authorization.expression('isAuthenticated()')}]]*/ false;

      if (!isAuthenticated) return;

      const user = {
        id: /*[[${#authentication.principal.id}]]*/ null,
        username: /*[[${#authentication.name}]]*/ 'Guest',
        clientType: /*[[${#authentication.principal.clientType}]]*/ null,
        avatarUrl: /*[[${#authentication.principal.avatarUrl}]]*/ null,
        lastLoginAt: /*[[${#authentication.principal.lastLoginAt}]]*/ null,
        groups: /*[[${#authentication.principal.groups}]]*/ [],
      };

      renderUserUI(user);
    }

    function renderUserUI(user) {
      const userNameEls = document.querySelectorAll('#userName');
      const avatarEls = document.querySelectorAll('#userAvatar, #userAvatarLarge');

      if (userNameEls.length > 0) {
        userNameEls.forEach((el) => (el.textContent = user.username));
      }

      const avatarUrl = user.avatarUrl || `https://ui-avatars.com/api/?name=${user.username}&background=random`;
      avatarEls.forEach((el) => (el.src = avatarUrl));

      // Set background image on userHeaderBg
      const headerBg = document.getElementById('userHeaderBg');
      if (headerBg) {
        headerBg.style.backgroundImage = `linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)), url('${avatarUrl}')`;
      }

      // Populate professional details
      const detailBox = document.getElementById('userInfoDetails');
      if (detailBox) {
        detailBox.style.display = 'flex';

        const detailName = document.getElementById('detailUserName');
        if (detailName) {
          detailName.textContent = user.username;
        }

        const typeBadge = document.getElementById('userTypeBadge');
        if (typeBadge) {
          typeBadge.textContent = user.clientType || 'N/A';
        }

        // Re-render icons inside detailBox
        if (window.renderIcons) {
          window.renderIcons();
        } else if (window.lucide) {
          lucide.createIcons();
        }
      }
    }

    function setActiveLink() {
      const path = window.location.pathname;
      const sidebarLinks = document.querySelectorAll('.app-sidebar .nav-link');

      sidebarLinks.forEach((link) => {
        const href = link.getAttribute('href');
        if (!href || href === '#' || href === '') return;

        if (path === href || (href !== '/' && path.startsWith(href))) {
          link.classList.add('active');

          const treeview = link.closest('.nav-treeview');
          if (treeview) {
            const parentItem = treeview.closest('.nav-item');
            if (parentItem) {
              parentItem.classList.add('menu-open');
              const parentLink = parentItem.querySelector(':scope > .nav-link');
              if (parentLink) parentLink.classList.add('active');
            }
          }
        }
      });
    }

    function bindEvents() {
      const logoutBtn = document.getElementById('logoutBtn');
      if (logoutBtn) {
        logoutBtn.addEventListener('click', async (e) => {
          e.preventDefault();

          const confirmResult = await Swal.fire({
            title: /*[[#{logout.confirm.title}]]*/ 'Are you sure?',
            text: /*[[#{logout.confirm.text}]]*/ 'You will be logged out of the session.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#0d6efd',
            cancelButtonColor: '#6c757d',
            confirmButtonText: /*[[#{logout.confirm.button}]]*/ 'Yes, log me out',
            cancelButtonText: /*[[#{logout.cancel.button}]]*/ 'Cancel',
          });

          if (confirmResult.isConfirmed) {
            window.location.href = '/logout';
          }
        });
      }
    }

    function init() {
      const menuContainer = document.getElementById('sidebarMenu');
      if (menuContainer) {
        menuContainer.innerHTML = renderMenu(MENU_CONFIG);
      }

      const sidebarWrapper = document.querySelector('.sidebar-wrapper');
      const isMobile = window.innerWidth <= 992;
      if (sidebarWrapper && typeof OverlayScrollbarsGlobal?.OverlayScrollbars !== 'undefined' && !isMobile) {
        OverlayScrollbarsGlobal.OverlayScrollbars(sidebarWrapper, {
          scrollbars: {
            theme: 'os-theme-light',
            autoHide: 'leave',
            clickScroll: true,
          },
        });
      }

      if (window.renderIcons) {
        window.renderIcons();
      } else if (window.lucide) {
        lucide.createIcons();
      }

      initUserSession();
      setActiveLink();
      bindEvents();
    }

    return {
      init,
    };
  }

  document.addEventListener('DOMContentLoaded', () => {
    const appLayout = Layout();
    appLayout.init();
  });
})();
