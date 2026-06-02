export class AccountPage {
    private orderElementsList: NodeListOf<HTMLDivElement>;

    constructor() {
        this.orderElementsList = document.querySelectorAll('.order-card');
    }

    init() {
        this.orderElementsList.forEach(orderElement => {
            const modalElements = orderElement.querySelectorAll<HTMLDivElement>('.whole-page-modal[data-trigger]');

            modalElements.forEach(modalElement => {
                if (modalElement && modalElement.dataset.trigger) {
                    const triggerElement = orderElement.querySelector(`.${modalElement.dataset.trigger}`);
                    const closeButton = modalElement.querySelector('.close-button');

                    const toggle = () => {
                        document.body.classList.toggle('no-scroll');
                        modalElement.classList.toggle('modal-visible')
                    };

                    triggerElement?.addEventListener('click', () => toggle());
                    closeButton?.addEventListener('click', () => toggle());
                }
            });
        });
    }
}