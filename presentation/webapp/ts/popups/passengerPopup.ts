import { Popup } from "./popup";

export class PassengersPopup extends Popup {
    private adults;
    private children;
    private currentClass;

    private readonly visibleInput: HTMLInputElement;

    get totalPassengers() {
        return this.adults + this.children;
    }

    private readonly adultCount: HTMLElement;
    private readonly childCount: HTMLElement;

    private readonly adultsHidden: HTMLInputElement;
    private readonly childrenHidden: HTMLInputElement;

    private readonly serviceClassRadioButtons: NodeListOf<HTMLInputElement>;

    constructor(element: HTMLElement, visibleInput: HTMLInputElement) {
        super(element);
        this.visibleInput = visibleInput;

        this.adultCount = document.getElementById('adult-passenger-count')!;
        this.childCount = document.getElementById('child-passenger-count')!;

        this.adultsHidden = document.getElementById('adults') as HTMLInputElement;
        this.childrenHidden = document.getElementById('children') as HTMLInputElement;

        const parsedAdults = parseInt(this.adultsHidden?.value ?? '');
        this.adults = !isNaN(parsedAdults) ? parsedAdults : 1;

        const parsedChildren = parseInt(this.childrenHidden?.value ?? '');
        this.children = !isNaN(parsedChildren) ? parsedChildren : 0;

        const checkedRadio = document.querySelector('input[name="serviceClass"]:checked') as HTMLInputElement | null;
        this.currentClass = checkedRadio?.labels?.[0]?.textContent?.trim() ?? checkedRadio?.value ?? 'Эконом';

        this.serviceClassRadioButtons = element.querySelectorAll('input[name="serviceClass"]');

        this.bindButtons();
        this.bindPassengerCounters();
        this.bindServiceClassRadioButtons();
        this.updateInput();
        this.bindResize();
    }

    protected override onOpen(): void {
        this.setLocation();
    }

    private bindResize(): void {
        window.addEventListener('resize', () => {
            if (this.isOpen) this.close();
        });
    }

    private setLocation() {
        const wrapper = this.visibleInput?.parentElement;

        this.element.style.top = `${this.visibleInput.offsetHeight + 15}px`;
        this.element.style.left = `${wrapper?.offsetLeft ?? this.visibleInput?.offsetLeft ?? 0}px`;
    }

    private bindPassengerCounters() {
        this.adultCount.textContent = String(this.adults);
        this.childCount.textContent = String(this.children);
    }

    private bindButtons(): void {
        this.element.querySelector('.adult .minus-button')?.addEventListener('click', () => this.change('adults', -1));
        this.element.querySelector('.adult .plus-button')?.addEventListener('click', () => this.change('adults', +1));
        this.element.querySelector('.child .minus-button')?.addEventListener('click', () => this.change('children', -1));
        this.element.querySelector('.child .plus-button')?.addEventListener('click', () => this.change('children', +1));
    }

    private bindServiceClassRadioButtons(): void {
        this.serviceClassRadioButtons.forEach(radio => {
            radio.addEventListener('change', () => {
                this.currentClass = radio.labels?.[0]?.textContent.trim() ?? radio.value;
                this.updateInput();
            });
        });
    }

    private change(type: 'adults' | 'children', delta: number): void {
        const total = this.totalPassengers;

        if (type === 'adults') {
            const next = this.adults + delta;
            if (next < 1 || total + delta > 9) return;
            this.adults = next;
        }
        else {
            const next = this.children + delta;
            if (next < 0 || total + delta > 9) return;
            this.children = next;
        }

        this.bindPassengerCounters();

        this.adultsHidden.value = String(this.adults);
        this.childrenHidden.value = String(this.children);

        this.updateInput();
    }

    private updateInput(): void {
        this.visibleInput.value = `${this.totalPassengers} ${this.pluralize(this.totalPassengers)}, ${this.currentClass}`;
    }

    private pluralize(n: number): string {
        if (n % 10 === 1 && n % 100 !== 11) return 'Пассажир';
        if (n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 10 || n % 100 >= 20)) return 'Пассажира';
        return 'Пассажиров';
    }
}