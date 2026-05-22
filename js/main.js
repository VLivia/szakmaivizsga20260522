// ===== FŐPROGRAM =====

/**
 * Alkalmazás inicializálása
 */
async function initApp() {
    showLoading();
    
    try {
        allChefs = await fetchChefs();
        displayChefs(allChefs);
        hideLoading();
        setupEventListeners();
    } catch (error) {
        console.error('Alkalmazás inicializálási hiba:', error);
        showError();
    }
}

/**
 * Event listenerek beállítása
 */
function setupEventListeners() {
    // Keresés
    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.querySelector('.search-btn');
    
    searchBtn.addEventListener('click', () => {
        filterChefs(searchInput.value);
    });
    
    searchInput.addEventListener('keyup', (e) => {
        if (e.key === 'Enter') {
            filterChefs(searchInput.value);
        } else {
            filterChefs(searchInput.value);
        }
    });

    // Modal gombok
    document.querySelector('.modal-close').addEventListener('click', closeRentalModal);
    document.getElementById('cancelBtn').addEventListener('click', closeRentalModal);

    // Dátum inputok - számítás frissítése
    document.getElementById('startDate').addEventListener('change', updateRentalCalculation);
    document.getElementById('endDate').addEventListener('change', updateRentalCalculation);

    // Bérlés form submit
    document.getElementById('rentalForm').addEventListener('submit', handleRentalSubmit);

    // Modal bezárása az X gombra
    window.addEventListener('click', (e) => {
        const modal = document.getElementById('rentalModal');
        if (e.target === modal) {
            closeRentalModal();
        }
    });
}

/**
 * Bérlés form submit kezelése
 */
async function handleRentalSubmit(e) {
    e.preventDefault();

    // Validáció
    const validation = validateRentalPeriod();
    if (!validation.valid) {
        showAlert(validation.message, 'error');
        return;
    }

    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const days = Math.ceil((new Date(endDate) - new Date(startDate)) / (1000 * 60 * 60 * 24)) + 1;

    const rentalData = {
        userId: 101,
        chefId: selectedChef.id,
        chefName: selectedChef.name,
        startDate: startDate,
        endDate: endDate,
        days: days,
        pricePerDay: selectedChef.pricePerDay,
        totalPrice: days * selectedChef.pricePerDay,
        timestamp: new Date().toISOString()
    };

    try {
        // Submit gomb letiltása
        const submitBtn = document.getElementById('submitBtn');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Feldolgozás...';

        // Bérlés küldése - Demo: csak localStorage-ba mentjük
        // Valós alkalmazásban: await submitRental(rentalData);
        saveRentalToLocalStorage(rentalData);

        showAlert(`✓ Bérlés sikeres! ${selectedChef.name} séf foglalva: ${days} napra`, 'success');
        
        // Modal bezárása
        setTimeout(() => {
            closeRentalModal();
            submitBtn.disabled = false;
            submitBtn.textContent = 'Bérlés Megerősítése';
        }, 1000);

    } catch (error) {
        console.error('Bérlés küldési hiba:', error);
        showAlert('Hiba a bérlés rögzítésekor. Kérjük, próbálja később.', 'error');
        const submitBtn = document.getElementById('submitBtn');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Bérlés Megerősítése';
    }
}

/**
 * Bérlés mentése localStorage-ba (demo célokra)
 */
function saveRentalToLocalStorage(rentalData) {
    const rentals = JSON.parse(localStorage.getItem('rentals') || '[]');
    rentals.push(rentalData);
    localStorage.setItem('rentals', JSON.stringify(rentals));
    console.log('Bérlés mentve:', rentalData);
}

// Alkalmazás indítása az oldal betöltéskor
document.addEventListener('DOMContentLoaded', initApp);
