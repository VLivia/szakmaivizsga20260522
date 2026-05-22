// ===== API MÓDOK =====

const API_URL = 'https://p161-7ddfd-default-rtdb.europe-west1.firebasedatabase.app/chefs.json';
const BACKEND_URL = 'http://localhost:3000'; // Háttérszerver URL

/**
 * Séfek lekérése a Firebase adatbázisból
 */
async function fetchChefs() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        return Array.isArray(data) ? data : Object.values(data);
    } catch (error) {
        console.error('Hiba a séfek lekérésekor:', error);
        throw error;
    }
}

/**
 * Bérlés rögzítése a háttérszerveren
 */
async function submitRental(rentalData) {
    try {
        const response = await fetch(`${BACKEND_URL}/rentals`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(rentalData),
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        return result;
    } catch (error) {
        console.error('Hiba a bérlés rögzítésekor:', error);
        throw error;
    }
}
