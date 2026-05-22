const toggleButton = document.querySelector('.menu-toggle');
const menuPanel = document.querySelector('.menu-panel');

if (toggleButton && menuPanel) {
  toggleButton.addEventListener('click', () => {
    const isOpen = menuPanel.classList.toggle('open');
    toggleButton.setAttribute('aria-expanded', String(isOpen));
  });

  menuPanel.querySelectorAll('a').forEach((link) => {
    link.addEventListener('click', () => {
      if (window.innerWidth < 992) {
        menuPanel.classList.remove('open');
        toggleButton.setAttribute('aria-expanded', 'false');
      }
    });
  });

  window.addEventListener('resize', () => {
    if (window.innerWidth >= 992) {
      menuPanel.classList.remove('open');
      toggleButton.setAttribute('aria-expanded', 'false');
    }
  });
}
