export class OffersPage {
    public init(): void {
        const filterButtons = document.querySelectorAll<HTMLAnchorElement>('.filter-button[data-filter]');
        if (!filterButtons.length) return;

        const url = new URL(window.location.href);

        filterButtons.forEach(filterButton => {
            const filter = filterButton.dataset.filter;
            if (!filter) return;

            url.searchParams.set('filter', filter);
            filterButton.href = url.toString();
        });
    }
}