export interface IPopup {
    open(): void;
    close(): void;
}

export abstract class Popup implements IPopup {
    protected readonly element: HTMLElement;
    private _isOpen = false;

    constructor(element: HTMLElement) {
        this.element = element;
    }

    get isOpen(): boolean {           
        return this._isOpen;
    }

    open(): void {
        this.element.classList.add('popup-visible');
        this._isOpen = true;
        this.onOpen();
    }

    close(): void {
        this.element.classList.remove('popup-visible');
        this._isOpen = false;
        this.onClose();
    }

    protected onOpen(): void {}
    protected onClose(): void {}
}