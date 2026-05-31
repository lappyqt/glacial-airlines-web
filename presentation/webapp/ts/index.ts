import "../css/index.css";
import { CheckoutPage } from "./pages/checkoutPage";
import { OffersPage } from "./pages/offersPage";
import { ServicesPage } from "./pages/servicesPage";
import { PopupManager } from "./popups/popupManager";

document.addEventListener('DOMContentLoaded', () => {
    new PopupManager().init();
    new OffersPage().init();

    if (document.querySelector('.seat-map')) {
        new ServicesPage();
    }

    if (document.querySelector('.aside.checkout')) {
        new CheckoutPage();
    }
});