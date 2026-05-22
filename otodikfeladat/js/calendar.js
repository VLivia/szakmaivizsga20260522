// ===== NAPTÁR FUNKCIÓK =====

/**
 * Naptár validáció - minimum 3 nap, maximum 14 nap bérlési időtartam
 */
function validateRentalPeriod() {
    const startDate = new Date(document.getElementById('startDate').value);
    const endDate = new Date(document.getElementById('endDate').value);

    if (!startDate || !endDate) {
        return { valid: false, message: 'Kérjük válasszon kezdési és befejezési dátumot' };
    }

    if (startDate > endDate) {
        return { valid: false, message: 'A kezdési dátum nem lehet későbbi a befejezési dátumnál' };
    }

    const days = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1;

    if (days < 3) {
        return { valid: false, message: 'A bérlési időtartam legalább 3 napnak kell lennie' };
    }

    if (days > 14) {
        return { valid: false, message: 'A bérlési időtartam maximum 14 nap lehet' };
    }

    return { valid: true };
}

/**
 * Dátum formázása ISO 8601 formátumban
 */
function formatDateISO(date) {
    return date.toISOString().split('T')[0];
}

/**
 * Dátum formázása ember-olvasható formátumban
 */
function formatDateHuman(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('hu-HU', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}
