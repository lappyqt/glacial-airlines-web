import "../css/index.css";
import { PopupManager } from "./popups/popupManager";

document.addEventListener('DOMContentLoaded', () => {
    new PopupManager().init();
});