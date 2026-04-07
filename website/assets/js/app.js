const docs = window.DOCS_DATA;
const body = document.body;
const contentRoot = document.getElementById('contentRoot');
const sidebarScroll = document.getElementById('sidebarScroll');
const menuToggle = document.getElementById('menuToggle');
const mobileOverlay = document.getElementById('mobileOverlay');
const searchInput = document.getElementById('searchInput');
const topbarTitle = document.getElementById('topbarTitle');
const tocContainer = document.getElementById('tocLinks');
const orderedPageIds = docs.groups.flatMap((group) => group.pages.map((page) => page.id));

function getPagerInfo(pageId, direction) {
  const currentIndex = orderedPageIds.indexOf(pageId);
  if (currentIndex === -1 || !orderedPageIds.length) {
    return null;
  }

  const offset = direction === 'prev' ? -1 : 1;
  const targetIndex = (currentIndex + offset + orderedPageIds.length) % orderedPageIds.length;
  const targetId = orderedPageIds[targetIndex];
  const targetPage = docs.pages[targetId];
  const fallbackTitle = docs.groups
    .flatMap((group) => group.pages)
    .find((page) => page.id === targetId)?.label ?? targetId;

  return {
    id: targetId,
    title: targetPage?.topbarTitle || fallbackTitle,
    desc: direction === 'prev' ? 'Go to the previous page in the sidebar.' : 'Go to the next page in the sidebar.'
  };
}

function renderSidebar() {
  const groupsMarkup = docs.groups
    .map((group) => `
      <div class="nav-group" data-group>
        <div class="nav-group-title">${group.title}</div>
        ${group.pages.map((page) => `<a class="nav-link" data-page-link="${page.id}" href="#/${page.id}">${page.label}</a>`).join('')}
      </div>
    `)
    .join('');

  sidebarScroll.innerHTML = `${groupsMarkup}<div class="empty-search" id="emptySearch">No pages matched your search.</div>`;
}

function renderPage(pageId) {
  const page = docs.pages[pageId] || docs.pages.index;
  const prevPage = getPagerInfo(pageId, 'prev') || page.prev;
  const nextPage = getPagerInfo(pageId, 'next') || page.next;
  const meta = page.meta.map((item) => `<span>${item}</span>`).join('<span>·</span>');

  contentRoot.innerHTML = `
    <article class="page-content" data-page="${pageId}">
      <div class="page-meta">${meta}</div>
      ${page.content}
      <div class="pager">
        <a class="pager-link prev" href="#/${prevPage.id}">
          <span class="pager-arrow">←</span>
          <div class="pager-eyebrow">Previous</div>
          <div class="pager-title">${prevPage.title}</div>
          <div class="pager-desc">${prevPage.desc}</div>
        </a>
        <a class="pager-link next" href="#/${nextPage.id}">
          <span class="pager-arrow">→</span>
          <div class="pager-eyebrow">Next</div>
          <div class="pager-title">${nextPage.title}</div>
          <div class="pager-desc">${nextPage.desc}</div>
        </a>
      </div>
    </article>
  `;

  document.title = page.title;
  topbarTitle.textContent = page.topbarTitle;
  setActiveNav(pageId);
  buildToc(pageId);
}

function buildToc(pageId) {
  const article = contentRoot.querySelector('.page-content');
  const sections = Array.from(article.querySelectorAll('section[id]'));
  tocContainer.innerHTML = sections.map((section, index) => {
    const heading = section.querySelector('h2');
    if (!heading) return '';
    const active = index === 0 ? ' active' : '';
    return `<a class="toc-link${active}" data-section-id="${section.id}" href="#/${pageId}/${section.id}">${heading.textContent.trim()}</a>`;
  }).join('');
}

function getPageFromHash() {
  const hash = window.location.hash || '#/index';
  const pageId = hash.replace(/^#\//, '').split('/')[0];
  return docs.pages[pageId] ? pageId : 'index';
}

function handleHash() {
  const hash = window.location.hash || '#/index';
  const parts = hash.replace(/^#\//, '').split('/').filter(Boolean);
  const pageId = docs.pages[parts[0]] ? parts[0] : 'index';
  const sectionId = parts[1] || null;

  renderPage(pageId);

  requestAnimationFrame(() => {
    if (sectionId) {
      const target = contentRoot.querySelector(`#${CSS.escape(sectionId)}`);
      if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
    syncActiveSection();
    window.scrollTo({ top: 0, behavior: 'auto' });
  });
}

function setActiveNav(pageId) {
  Array.from(document.querySelectorAll('[data-page-link]')).forEach((link) => {
    link.classList.toggle('active', link.dataset.pageLink === pageId);
  });
}

function syncActiveSection() {
  const sections = Array.from(contentRoot.querySelectorAll('section[id]'));
  if (!sections.length) return;
  const offset = window.scrollY + 130;
  let currentId = sections[0].id;

  sections.forEach((section) => {
    if (section.offsetTop <= offset) currentId = section.id;
  });

  Array.from(tocContainer.querySelectorAll('.toc-link')).forEach((link) => {
    link.classList.toggle('active', link.dataset.sectionId === currentId);
  });
}

function filterSidebar(query) {
  const normalized = query.trim().toLowerCase();
  const groups = Array.from(document.querySelectorAll('[data-group]'));
  const emptySearch = document.getElementById('emptySearch');
  let visibleCount = 0;

  groups.forEach((group) => {
    const links = Array.from(group.querySelectorAll('.nav-link'));
    let groupVisible = 0;
    links.forEach((link) => {
      const match = link.textContent.toLowerCase().includes(normalized);
      const show = !normalized || match;
      link.style.display = show ? '' : 'none';
      if (show) {
        visibleCount += 1;
        groupVisible += 1;
      }
    });
    group.style.display = groupVisible ? '' : 'none';
  });

  emptySearch.style.display = visibleCount ? 'none' : 'block';
}

function closeSidebar() {
  body.classList.remove('sidebar-open');
}

renderSidebar();
handleHash();

if (menuToggle) {
  menuToggle.addEventListener('click', () => body.classList.toggle('sidebar-open'));
}
if (mobileOverlay) {
  mobileOverlay.addEventListener('click', closeSidebar);
}
if (searchInput) {
  searchInput.addEventListener('input', (event) => filterSidebar(event.target.value));
}

window.addEventListener('hashchange', handleHash);
window.addEventListener('scroll', syncActiveSection, { passive: true });
window.addEventListener('resize', () => {
  if (window.innerWidth > 1024) closeSidebar();
});

document.addEventListener('click', (event) => {
  if (event.target.matches('[data-page-link]') && window.innerWidth <= 1024) {
    closeSidebar();
  }
});
