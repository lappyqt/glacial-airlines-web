export class ServicesPage {
    private readonly seats: NodeListOf<HTMLElement>;
    private readonly passengerBlocks: NodeListOf<HTMLElement>;
    private readonly serviceCheckboxes: NodeListOf<HTMLInputElement>;
    private readonly servicesPriceElement: HTMLElement | null;
    private readonly seatsPriceElement: HTMLElement | null;
    private readonly totalPriceElement: HTMLElement | null;

    private activePassengerIndex: number = 0;
    private selectedSeats: Map<number, { seatId: string, seatLabel: string, isEmergency: boolean }> = new Map();
    private basePrice: number = 0;
    
    private readonly emergencySurcharge = 400;

    constructor() {
        this.seats = document.querySelectorAll<HTMLElement>('.seat');
        this.passengerBlocks = document.querySelectorAll<HTMLElement>('.seat-selection-block');
        this.serviceCheckboxes = document.querySelectorAll<HTMLInputElement>('input[name="selectedServiceIds"]');

        const priceLines = document.querySelectorAll<HTMLElement>('.prices .price-line .price');
        this.seatsPriceElement = priceLines[1] ?? null;
        this.servicesPriceElement = priceLines[2] ?? null;
        this.totalPriceElement = document.querySelector('.price-line.final .price');

        const aside = document.querySelector<HTMLElement>('.aside');
        this.basePrice = parseInt(aside?.dataset.basePrice ?? '0', 10);

        this.restoreState();
        this.bindPassengers();
        this.bindSeats();
        this.bindServices();
        this.setActivePassenger(0);
        this.updateTotal();

        window.addEventListener('pageshow', (event) => {
            if (event.persisted) {
                this.restoreState();
                this.updateTotal();
            }
        });
    }

    private restoreState(): void {
        this.passengerBlocks.forEach((_, index) => {
            document.querySelector('#skipSeats').value = false;

            const input = document.getElementById(`outbound-seat-${index}`) as HTMLInputElement | null;
            const label = document.getElementById(`seat-label-${index}`);

            if (!input?.value) return;

            const matchingSeat = Array.from(this.seats).find(s => s.dataset.seatId === input.value);
            if (!matchingSeat) return;

            const isEmergency = matchingSeat.dataset.emergency === 'true';
            const seatLabel = matchingSeat.dataset.seatLabel ?? label?.textContent?.trim() ?? '';

            this.selectedSeats.set(index, { seatId: input.value, seatLabel, isEmergency });

            if (label) label.textContent = seatLabel;

            matchingSeat.classList.remove('free', 'emergency');
            matchingSeat.classList.add('selected');
        });
    }

    private bindPassengers(): void {
        this.passengerBlocks.forEach((block, index) => {
            const btn = block.querySelector('.seat-select-button');
            btn?.addEventListener('click', () => this.setActivePassenger(index));
        });
    }

    private setActivePassenger(index: number): void {
        this.activePassengerIndex = index;
        this.passengerBlocks.forEach((block, i) => {
            block.classList.toggle('active', i === index);

            const selectButton = block.querySelector('.seat-select-button');
            if (selectButton) {
                selectButton.textContent = i === index ? 'Выбирается...' : (this.selectedSeats.has(i) ? 'Изменить место' : 'Выбрать место');
            }
        });
    }

    private bindSeats(): void {
        this.seats.forEach(seat => {
            seat.addEventListener('click', () => this.selectSeat(seat));
        });
    }

    private selectSeat(seat: HTMLElement): void {
        if (seat.classList.contains('taken') || seat.classList.contains('disabled')) return;

        const seatId = seat.dataset.seatId ?? '';
        const seatLabel = seat.dataset.seatLabel ?? '';
        const isEmergency = seat.dataset.emergency === 'true';
        const index = this.activePassengerIndex;
        const prev = this.selectedSeats.get(index);

        if (prev) {
            this.seats.forEach(s => {
                if (s.dataset.seatId === prev.seatId) {
                    s.classList.remove('selected');
                    s.classList.add(s.dataset.emergency === 'true' ? 'emergency' : 'free');
                }
            });
        }

        seat.classList.remove('free', 'emergency');
        seat.classList.add('selected');

        this.selectedSeats.set(index, { seatId, seatLabel, isEmergency });

        const input = document.getElementById(`outbound-seat-${index}`) as HTMLInputElement | null;
        const label = document.getElementById(`seat-label-${index}`);
        if (input) input.value = seatId;
        if (label) label.textContent = seatLabel;

        this.updateTotal(); 

        const next = index + 1;
        if (next < this.passengerBlocks.length) {
            this.setActivePassenger(next);
        }
    }

    private bindServices(): void {
        this.serviceCheckboxes.forEach(checkbox => {
            checkbox.addEventListener('change', () => this.updateTotal());
        });
    }

    private updateTotal(): void {
        let servicesTotal = 0;
        let seatsTotal = 0;

        this.serviceCheckboxes.forEach(checkbox => {
            if (checkbox.checked) {
                const price = parseInt(checkbox.dataset.price?.replace(/\s/g, '') ?? '0', 10);
                servicesTotal += price;
            }
        });

        this.selectedSeats.forEach(seat => {
            if (seat.isEmergency) seatsTotal += this.emergencySurcharge;
        });

        if (this.seatsPriceElement) {
            this.seatsPriceElement.textContent = `${seatsTotal.toLocaleString('ru-RU')} RUB`;
        }

        if (this.servicesPriceElement) {
            this.servicesPriceElement.textContent = `${servicesTotal.toLocaleString('ru-RU')} RUB`;
        }

        if (this.totalPriceElement) {
            const total = this.basePrice + servicesTotal + seatsTotal;
            this.totalPriceElement.textContent = `${total.toLocaleString('ru-RU')} RUB`;
        }
    }
}