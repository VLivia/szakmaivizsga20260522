// ===== UI KEZELÉS =====

let allChefs = [];
let selectedChef = null;

/**
 * Séf kártyák megjelenítése
 */
function displayChefs(chefs) {
    const chefsList = document.getElementById('chefsList');
    chefsList.innerHTML = '';

    if (chefs.length === 0) {
        chefsList.innerHTML = '<p style="grid-column: 1/-1; text-align: center; color: var(--text-light);">Nincs találat</p>';
        return;
    }

    chefs.forEach(chef => {
        const card = createChefCard(chef);
        chefsList.appendChild(card);
    });
}

/**
 * Egyedi séf kártya létrehozása
 */
function createChefCard(chef) {
    const card = document.createElement('div');
    card.className = 'chef-card';
    card.innerHTML = `
        <div class="chef-header">
            <h3 class="chef-name">${chef.name}</h3>
            <span class="chef-cuisine">${chef.cuisine}</span>
        </div>
        <div class="chef-prices">
            <div class="price-item">
                <span class="price-label">Ár/nap</span>
                <span class="price-value">${chef.pricePerDay} €</span>
            </div>
            <div class="price-item">
                <span class="price-label">Min. 3 nap</span>
                <span class="price-value">${chef.pricePerDay * 3} €</span>
            </div>
        </div>
        <button class="btn btn-rent" data-chef-id="${chef.id}">Bérlés</button>
    `;

    card.querySelector('.btn-rent').addEventListener('click', () => {
        selectedChef = chef;
        openRentalModal(chef);
    });

    return card;
}

/**
 * Bérlési modal megnyitása
 */
function openRentalModal(chef) {
    document.getElementById('chefName').textContent = chef.name;
    document.getElementById('chefCuisine').textContent = chef.cuisine;
    document.getElementById('pricePerDay').textContent = `${chef.pricePerDay} €`;
    document.getElementById('rentalModal').classList.remove('hidden');
    
    // Alapértelmezett dátumok beállítása
    const today = new Date();
    const threeDaysLater = new Date(today);
    threeDaysLater.setDate(threeDaysLater.getDate() + 3);

    document.getElementById('startDate').valueAsDate = today;
    document.getElementById('endDate').valueAsDate = threeDaysLater;

    updateRentalCalculation();
}

/**
 * Bérlési modal bezárása
 */
function closeRentalModal() {
    document.getElementById('rentalModal').classList.add('hidden');
    document.getElementById('rentalForm').reset();
    selectedChef = null;
}

/**
 * Bérlési összeget és időtartamot kiszámoló
 */
function updateRentalCalculation() {
    const startDate = new Date(document.getElementById('startDate').value);
    const endDate = new Date(document.getElementById('endDate').value);

    if (!startDate || !endDate || startDate > endDate) {
        document.getElementById('rentalDays').textContent = '- nap';
        document.getElementById('totalPrice').textContent = '0 €';
        return;
    }

    const days = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1;
    const total = days * selectedChef.pricePerDay;

    document.getElementById('rentalDays').textContent = `${days} nap`;
    document.getElementById('totalPrice').textContent = `${total} €`;
}

/**
 * Figyelmeztetés megjelenítése
 */
function showAlert(message, type = 'success') {
    const alertContainer = document.getElementById('alertContainer');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    
    // HTML escapeelése a XSS megelőzésére
    const escapedMessage = escapeHtml(message);
    
    alert.innerHTML = `
        <p>${escapedMessage}</p>
        <button class="alert-close" aria-label="Bezárás">&times;</button>
    `;

    alertContainer.appendChild(alert);

    alert.querySelector('.alert-close').addEventListener('click', () => {
        alert.remove();
    });

    // Automatikus eltávolítás - sikeres: 5 sec, hiba: 8 sec
    const duration = type === 'error' ? 8000 : 5000;
    setTimeout(() => {
        if (alert.parentNode) {
            alert.remove();
        }
    }, duration);
}

/**
 * HTML karakterek escapeelése
 */
function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

/**
 * Séfek szűrése keresési feltétel alapján
 */
function filterChefs(query) {
    const filtered = allChefs.filter(chef => 
        chef.name.toLowerCase().includes(query.toLowerCase()) ||
        chef.cuisine.toLowerCase().includes(query.toLowerCase())
    );
    displayChefs(filtered);
}

/**
 * Loading spinner megjelenítése
 */
function showLoading() {
    document.getElementById('loadingSpinner').classList.remove('hidden');
    document.getElementById('errorMessage').classList.add('hidden');
}

/**
 * Loading spinner elrejtése
 */
function hideLoading() {
    document.getElementById('loadingSpinner').classList.add('hidden');
}

/**
 * Hiba megjelenítése
 */
function showError() {
    document.getElementById('errorMessage').classList.remove('hidden');
    document.getElementById('loadingSpinner').classList.add('hidden');
}
