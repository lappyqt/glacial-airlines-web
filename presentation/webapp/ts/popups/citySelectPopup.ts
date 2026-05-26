import { Popup } from "./popup";

export class CitySelectPopup extends Popup {
    private readonly rows: NodeListOf<HTMLElement>;

    private _activeTrigger: HTMLInputElement | null = null;

    private outboundAirportVisibleInput: HTMLInputElement | null;
    private returnAirportVisibleInput: HTMLInputElement | null;

    private outboundAirportHiddenInput: HTMLInputElement | null;
    private returnAirportHiddenInput: HTMLInputElement | null;

    constructor(element: HTMLElement) {
        super(element);

        this.outboundAirportVisibleInput = document.getElementById('from-visible');
        this.returnAirportVisibleInput = document.getElementById('to-visible');

        this.outboundAirportHiddenInput = document.getElementById('outboundAirportId');
        this.returnAirportHiddenInput = document.getElementById('returnAirportId');

        this.rows = element.querySelectorAll<HTMLElement>('.row');

        this.initInputs();
        this.bindRows();
        this.bindResize();
    }

    get activeTrigger(): HTMLInputElement | null {
        return this._activeTrigger;
    }

    setTrigger(trigger: HTMLInputElement): void {
        this._activeTrigger = trigger;
    }

    private initInputs(): void {
        const airportMap = new Map<string, string>();

        this.rows.forEach(row => {
            const id = row.getAttribute("data-airport-id");
            if (id) {
                const city = row.querySelector(".city")?.textContent?.trim() ?? '';
                const airport = row.querySelector(".airport")?.textContent?.trim() ?? '';
                airportMap.set(id, `${city} (${airport})`);
            }
        });

        const getResultStringFromRow = (id: string): string => {
            const row = [...this.rows].find(row => row.getAttribute("data-airport-id") === id);
            const city = row.querySelector(".city").textContent;
            const airport = row.querySelector(".airport").textContent;

            return `${city} (${airport})`;
        };

        const outboundAirportId = this.outboundAirportHiddenInput?.value.trim() ?? '';

        if (this.outboundAirportVisibleInput) {
            this.outboundAirportVisibleInput.value = airportMap.get(outboundAirportId) ?? '';
        }

        const returnAirportId = this.returnAirportHiddenInput?.value.trim() ?? '';
        if (this.returnAirportVisibleInput) {
            this.returnAirportVisibleInput.value = airportMap.get(returnAirportId) ?? '';
        }
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
        const wrapper = this.activeTrigger?.parentElement;
        this.element.style.top = `${(this.activeTrigger?.offsetHeight ?? 0) + 15}px`;
        this.element.style.left = `${wrapper?.offsetLeft ?? this.activeTrigger?.offsetLeft ?? 0}px`;
    }
}

