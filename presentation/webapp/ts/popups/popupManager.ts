import { CitySelectPopup } from "./citySelectPopup";
import { PassengersPopup } from "./passengerPopup";
import type { Popup } from "./popup";

export class PopupManager {
    private openPopup: Popup | null = null;
    private currentTrigger: HTMLInputElement | null = null;

    init(): void {
        this.initCitySelectPopups();
        this.bindOutsideClick();
        this.initPassengersPopup();
    }

    private initCitySelectPopups(): void {
        const triggers = document.querySelectorAll<HTMLInputElement>('input[data-popup="city-select"]');

        if (triggers.length === 0) return;

         const popupElement = document.querySelector<HTMLElement>('.city-select-popup');

        if (!popupElement) {
            console.warn('[PopupManager] .city-select-popup не найден');
            return;
        }

        const popup = new CitySelectPopup(popupElement);

        triggers.forEach(trigger => {
            trigger.addEventListener('click', (event) => {
                event.stopPropagation();
                this.toggleCitySelect(popup, trigger);
            });
        });
    }

    private initPassengersPopup() {
        const trigger = document.querySelector<HTMLInputElement>('.search-input.passengers');
        if (!trigger) return;

        const popupElement = document.querySelector<HTMLElement>('.passengers-popup');
        if (!popupElement) return;

        const popup = new PassengersPopup(popupElement, trigger);

        trigger.addEventListener('click', (event) => {
            event.stopPropagation();
            this.toggle(popup);
        });
    }

    private toggle(popup: Popup): void {
        if (popup.isOpen) {
            popup.close();
            this.openPopup = null;
            return;
        }

        this.openPopup?.close();
        popup.open();
        this.openPopup = popup;
    }

    private toggleCitySelect(popup: CitySelectPopup, trigger: HTMLInputElement): void {
        const isSameTrigger = popup.activeTrigger === trigger;

        if (popup.isOpen && isSameTrigger) {
            popup.close();
            this.openPopup = null;
            return;
        }

        this.openPopup?.close();
        popup.setTrigger(trigger);
        popup.open();
        this.openPopup = popup;
    }

    private bindOutsideClick(): void {
        document.addEventListener('click', (event) => {
            if (!this.openPopup) return;

            const target = event.target as HTMLElement;
            const popupElement = document.querySelector('.popup-visible');

            if (popupElement?.contains(target)) return;
            if (target.closest('[data-popup]')) return;

            this.openPopup.close();
            this.openPopup = null;
        });
    }
}