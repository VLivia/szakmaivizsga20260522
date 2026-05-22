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

        // Bérlés küldése - Háttérszerverre (vagy localStorage fallback)
        await submitRentalToBackend(rentalData);

        showAlert(`✓ Bérlés sikeres! ${selectedChef.name} séf foglalva ${days} napra (${formatDateHuman(startDate)} - ${formatDateHuman(endDate)})`, 'success');
        
        // Modal bezárása és forma reset
        setTimeout(() => {
            closeRentalModal();
            submitBtn.disabled = false;
            submitBtn.textContent = 'Bérlés Megerősítése';
        }, 1500);

    } catch (error) {
        console.error('Bérlés küldési hiba:', error);
        showAlert(`✗ Hiba a bérlés rögzítésekor: ${error.message}`, 'error');
        const submitBtn = document.getElementById('submitBtn');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Bérlés Megerősítése';
    }
}

/**
 * Bérlés küldése háttérszerverre vagy localStorage fallback
 */
async function submitRentalToBackend(rentalData) {
    try {
        // Próbálkozás a háttérszerver eléréséhez
        const response = await fetch(`${BACKEND_URL}/rentals`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(rentalData),
        });

        if (response.ok) {
            const result = await response.json();
            console.log('Bérlés sikeres a háttérszerveren:', result);
            return result;
        } else {
            throw new Error('Háttérszerver hiba');
        }
    } catch (backendError) {
        // Fallback: localStorage mentés
        console.warn('Háttérszerver nem elérhető, mentés localStorage-ba:', backendError.message);
        saveRentalToLocalStorage(rentalData);
        return { success: true, stored: 'local' };
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
