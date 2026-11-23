document.querySelectorAll('[data-amenity-list]').forEach(list => {
    const filter = list.querySelector('[data-amenity-filter]');
    if (!filter) {
        return;
    }
    const input = filter.querySelector('[data-amenity-search]');
    const items = Array.from(list.querySelectorAll('[data-amenity-item]'));
    const empty = list.querySelector('[data-amenity-no-results]');
    const normalize = text => text.toLocaleLowerCase('vi').normalize('NFD')
        .replace(/\p{M}/gu, '').replaceAll('đ', 'd').trim();
    const names = items.map(item => normalize(item.querySelector('[data-amenity-name]').textContent));
    filter.hidden = false;

    input.addEventListener('input', () => {
        const query = normalize(input.value);
        let matches = 0;
        items.forEach((item, index) => {
            const found = names[index].includes(query);
            item.hidden = !found;
            matches += Number(found);
        });
        empty.hidden = matches !== 0;
    });
});
