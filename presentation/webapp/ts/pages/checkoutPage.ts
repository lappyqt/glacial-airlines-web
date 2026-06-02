export class CheckoutPage {
    private readonly totalPrice: number;
    private readonly milesCount: number;

    private totalPriceElement: HTMLDivElement | null;
    private milesSpentElement: HTMLDivElement | null;
    private payWithMilesCheckbox: HTMLInputElement | null;

    constructor() {
        this.totalPriceElement = document.querySelector('.price-line.final > .price');
        this.milesSpentElement = document.querySelector('.price-line.miles-spent > .price');
        this.payWithMilesCheckbox = document.querySelector('input#payWithMiles');

        const datasetContainer = document.querySelector<HTMLDivElement>('[data-total-price]');

        this.totalPrice = Number(datasetContainer?.dataset.totalPrice);
        this.milesCount = Number(datasetContainer?.dataset.milesCount);

        this.payWithMilesCheckbox?.addEventListener('change', () => this.updateTotal());
        this.updateTotal();
    }

    private updateTotal() {
        const finalPrice = (this.payWithMilesCheckbox?.checked)
            ? ((this.totalPrice - this.milesCount) >= 0) ? this.totalPrice - this.milesCount : 0 
            : this.totalPrice;

        if (this.milesSpentElement !== null && this.totalPriceElement !== null) {  
            this.totalPriceElement.textContent = `${finalPrice} RUB`;
            this.milesSpentElement.textContent = '-';

            if (this.payWithMilesCheckbox?.checked) {
                this.milesSpentElement.textContent = `${this.milesCount}`;
            }
        }
    }
}