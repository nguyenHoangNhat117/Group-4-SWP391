(() => {
    document.querySelectorAll('[data-revenue-chart]').forEach(section => {
        const chart = section.querySelector('.revenue-chart');
        if (!chart) return;

        const metric = chart.querySelector('select');
        const bars = chart.querySelector('.revenue-chart-bars');
        const axis = chart.querySelector('.revenue-chart-axis');
        const total = chart.querySelector('.revenue-chart-total strong');
        const summary = chart.querySelector('.revenue-chart-value');
        const records = Array.from(section.querySelectorAll('[data-revenue-period]'), row => ({
            period: row.dataset.revenuePeriod,
            net: Number(row.dataset.net),
            collected: Number(row.dataset.collected),
            refunded: Number(row.dataset.refunded)
        }));
        if (records.length === 0) return;
        let selected = 0;

        const buttons = records.map((record, index) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'revenue-chart-period';

            const plot = document.createElement('span');
            plot.className = 'revenue-chart-plot';
            plot.setAttribute('aria-hidden', 'true');
            const column = document.createElement('span');
            column.className = 'revenue-chart-column';
            plot.append(column);

            const label = document.createElement('span');
            label.className = 'revenue-chart-period-label';
            label.textContent = record.period;
            button.append(plot, label);
            button.addEventListener('click', () => {
                selected = index;
                updateSelection();
            });
            return button;
        });
        bars.append(...buttons);

        function updateSelection() {
            buttons.forEach((button, index) => button.setAttribute('aria-pressed', String(index === selected)));
            summary.textContent = buttons[selected].title;
        }

        function render() {
            const label = metric.selectedOptions[0].textContent.trim();
            const locale = 'vi-VN';
            const format = new Intl.NumberFormat(locale, {style: 'currency', currency: 'VND'});
            const formatTick = new Intl.NumberFormat(locale, {notation: 'compact', maximumFractionDigits: 1});
            const values = records.map(record => record[metric.value]);
            const min = Math.min(0, ...values);
            const max = Math.max(0, ...values);
            const range = max - min;
            const zero = range ? 100 * max / range : 100;
            bars.style.setProperty('--chart-zero', zero + '%');
            total.textContent = format.format(values.reduce((sum, value) => sum + value, 0));
            const ticks = (range ? [0, 25, 50, 75, 100] : [100]).map(position => {
                const tick = document.createElement('span');
                tick.style.top = position + '%';
                tick.textContent = formatTick.format(range ? max - range * position / 100 : 0);
                return tick;
            });
            axis.replaceChildren(...ticks);

            buttons.forEach((button, index) => {
                const value = values[index];
                const point = range ? 100 * (max - value) / range : zero;
                const height = Math.abs(point - zero);
                button.style.setProperty('--column-top', Math.min(point, zero) + '%');
                button.style.setProperty('--column-height', height + '%');
                button.dataset.negative = String(value < 0);
                button.title = records[index].period + ' · ' + label + ' · ' + format.format(value);
                button.setAttribute('aria-label', button.title);
            });
            updateSelection();
        }

        metric.addEventListener('change', render);
        render();
        chart.hidden = false;
        section.querySelector('.revenue-chart-data').open = false;
    });
})();
