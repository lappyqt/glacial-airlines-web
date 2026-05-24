import { Popup } from "./popup";

export class CitySelectPopup extends Popup {
    private readonly rows: NodeListOf<HTMLElement>;

    private _activeTrigger: HTMLInputElement | null = null;

    constructor(element: HTMLElement) {
        super(element);

        this.rows = element.querySelectorAll<HTMLElement>('.row');
        this.bindRows();
        this.bindResize();
    }

    get activeTrigger(): HTMLInputElement | null {
        return this._activeTrigger;
    }

    setTrigger(trigger: HTMLInputElement): void {
        this._activeTrigger = trigger;
    }

    private bindRows(): void {
        this.rows.forEach(row => {
            row.addEventListener('click', () => this.selectRow(row));
        });
    }

    private bindResize(): void {
        window.addEventListener('resize', () => {
            if (this.isOpen) this.close();
        });
    }

    private selectRow(row: HTMLElement): void {
        if (!this.activeTrigger) return;

        const city = row.querySelector<HTMLElement>('.city')?.textContent?.trim() ?? '';
        const airportCode = row.querySelector<HTMLElement>('.airport')?.textContent?.trim() ?? '';
        const targetId = this.activeTrigger.dataset.target!;
        const hiddenInput = document.getElementById(targetId) as HTMLInputElement | null;

        const newVisibleValue = `${city} (${airportCode})`;
        const newHiddenValue = row.dataset.airportId ?? airportCode;

        document.querySelectorAll<HTMLInputElement>('input[data-popup="city-select"]').forEach(input => {
            if (input !== this.activeTrigger && input.value === newVisibleValue) {
                const otherId = input.dataset.target!;
                const otherHidden = document.getElementById(otherId) as HTMLInputElement | null;

                input.value = this.activeTrigger!.value;
                if (otherHidden) otherHidden.value = hiddenInput?.value ?? '';
            }
        });

        this.activeTrigger.value = newVisibleValue;
        if (hiddenInput) hiddenInput.value = newHiddenValue;

        this.close();
    }

    protected override onOpen(): void {
        this.setPosition();
    }

    private setPosition(): void {
        this.element.style.top = `${(this.activeTrigger?.offsetHeight ?? 0) + 15}px`;
        this.element.style.left = `${this.activeTrigger?.offsetLeft}px`;
    }
}

