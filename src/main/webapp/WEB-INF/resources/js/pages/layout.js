(function () {
  'use strict';
  const MENU_CONFIG = [
    {
      type: 'tree',
      label: /*[[#{menu.dashboard}]]*/ 'Dashboard',
      icon: 'layout-dashboard',
      children: [
        {
          label: /*[[#{index.title}]]*/ 'Analytics',
          icon: 'bar-chart-2',
          link: '/',
        },
        {
          label: /*[[#{menu.location}]]*/ 'Location Info',
          icon: 'info',
          link: '#',
        },
      ],
    },
    {
      type: 'header',
      label: /*[[#{menu.management}]]*/ 'Management',
    },
    {
      type: 'tree',
      label: /*[[#{menu.location}]]*/ 'Location',
      icon: 'map-pin',
      children: [
        {
          label: /*[[#{menu.rooms}]]*/ 'Rooms',
          icon: 'door-open',
          link: '#',
        },
        {
          label: /*[[#{menu.floors}]]*/ 'Floors',
          icon: 'layers',
          link: '#',
        },
      ],
    },
    {
      type: 'tree',
      label: /*[[#{menu.access_control}]]*/ 'Access Control',
      icon: 'shield-check',
      children: [
        {
          label: /*[[#{menu.clients}]]*/ 'Clients',
          icon: 'contact',
          link: '#',
        },
        {
          label: /*[[#{menu.functions}]]*/ 'Functions',
          icon: 'key',
          link: '#',
        },
        {
          label: /*[[#{menu.roles}]]*/ 'Roles',
          icon: 'badge-check',
          link: '#',
        },
        {
          label: /*[[#{menu.groups}]]*/ 'Groups',
          icon: 'users',
          link: '#',
        },
      ],
    },
    {
      type: 'header',
      label: /*[[#{menu.smart_system}]]*/ 'Smart System',
    },
    {
      label: /*[[#{menu.automations}]]*/ 'Automations',
      icon: 'zap',
      link: '#',
    },
    {
      label: /*[[#{menu.rules}]]*/ 'Rules',
      icon: 'git-branch',
      link: '#',
    },
  ];

  function Layout() {
    function renderMenu(items) {
      return items
        .map((item) => {
          switch (item.type) {
            case 'header':
              return `<li class="nav-header">${item.label}</li>`;
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
                        ${item.children.map((child) => renderLink(child, true)).join('')}
                    </ul>
                </li>
            `;
    }

    function initUserSession() {
      const isAuthenticated = /*[[${#authorization.expression('isAuthenticated()')}]]*/ false;

      if (!isAuthenticated) return;

      const user = {
        username: /*[[${#authentication.name}]]*/ 'Guest',
        avatarUrl: null,
      };

      renderUserUI(user);
    }

    function renderUserUI(user) {
      const userNameEls = document.querySelectorAll('#userName, #userFullName');
      const avatarEls = document.querySelectorAll('#userAvatar, #userAvatarLarge');

      if (userNameEls.length > 0) {
        userNameEls.forEach((el) => (el.textContent = user.username));
      }

      const avatarUrl =
        user.avatarUrl || `https://ui-avatars.com/api/?name=${user.username}&background=random`;
      avatarEls.forEach((el) => (el.src = avatarUrl));
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
