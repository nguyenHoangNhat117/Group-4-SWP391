/* Luxe Escape dashboard charts. Presentation only; all values come from the server. */
(function () {
    'use strict';
    const root = document.getElementById('hotel-dashboard');
    if (!root || !document.getElementById('hd-data')) return;
    const ns = 'http://www.w3.org/2000/svg';
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches || document.body.classList.contains('disable-animation');
    const whole = new Intl.NumberFormat('vi-VN', {maximumFractionDigits: 0});
    const decimal = new Intl.NumberFormat('vi-VN', {maximumFractionDigits: 2});
    const compact = new Intl.NumberFormat('vi-VN', {notation: 'compact', maximumFractionDigits: 1});
    const labels = {pending:'Pending', confirmed:'Confirmed', cancelled:'Cancelled', checked_in:'Checked in', checked_out:'Checked out', completed:'Completed', paid:'Paid', failed:'Failed', refunded:'Refunded', available:'Available', maintenance:'Maintenance', out_of_service:'Out of service'};
    const colors = {pending:'#cfb16f', confirmed:'#91a395', cancelled:'#bc8972', checked_in:'#596f60', checked_out:'#9a94aa', completed:'#a58a64', paid:'#8c9f8f', failed:'#bc8972', refunded:'#9a94aa', available:'#7c9182', maintenance:'#c8a76c', out_of_service:'#bc8972'};
    const num = value => Number.isFinite(Number(value)) ? Number(value) : 0;
    const money = value => decimal.format(value) + ' ₫';
    const name = key => labels[key] || key;
    const color = key => colors[key] || '#b29a7b';
    const read = id => Array.from(document.querySelectorAll('#' + id + ' > span')).map(el => ({label:el.dataset.label, value:num(el.dataset.value), count:num(el.dataset.count)}));
    function svg(tag, attrs, text) {
        const el = document.createElementNS(ns, tag);
        Object.entries(attrs || {}).forEach(([key, value]) => el.setAttribute(key, value));
        if (text !== undefined) el.textContent = text;
        return el;
    }
    function node(tag, className, text) {
        const el = document.createElement(tag);
        if (className) el.className = className;
        if (text !== undefined) el.textContent = text;
        return el;
    }
    function animate(duration, update, valid) {
        if (reduced) { update(1); return; }
        const start = performance.now();
        function step(now) {
            if (valid && !valid()) return;
            const t = Math.min((now - start) / duration, 1);
            update(1 - Math.pow(1 - t, 3));
            if (t < 1) requestAnimationFrame(step);
        }
        requestAnimationFrame(step);
    }
    function visible(el, callback) {
        if (!el) return;
        if (reduced || !('IntersectionObserver' in window)) { callback(); return; }
        const observer = new IntersectionObserver(entries => {
            if (entries.some(entry => entry.isIntersecting)) { observer.disconnect(); callback(); }
        }, {threshold:0.08});
        observer.observe(el);
    }
    // Use a shared accessible tooltip for pointer and keyboard chart interaction.
    const tooltip = node('div', 'hd-tooltip');
    tooltip.id = 'hd-chart-tooltip';
    tooltip.setAttribute('role', 'tooltip');
    tooltip.hidden = true;
    root.appendChild(tooltip);
    function moveTooltip(x, y) {
        const box = tooltip.getBoundingClientRect();
        tooltip.style.left = Math.max(10, Math.min(x + 12, innerWidth - box.width - 12)) + 'px';
        tooltip.style.top = Math.max(10, Math.min(y + 14, innerHeight - box.height - 12)) + 'px';
    }
    function interactive(el, text) {
        el.classList.add('hd-chart-mark');
        el.setAttribute('tabindex', '0');
        el.setAttribute('role', 'img');
        el.setAttribute('aria-label', text);
        el.appendChild(svg('title', {}, text));
        function show(x, y) { tooltip.textContent = text; tooltip.hidden = false; moveTooltip(x, y); el.setAttribute('aria-describedby', tooltip.id); }
        function hide() { tooltip.hidden = true; el.removeAttribute('aria-describedby'); }
        el.addEventListener('pointerenter', event => show(event.clientX, event.clientY));
        el.addEventListener('pointermove', event => moveTooltip(event.clientX, event.clientY));
        el.addEventListener('pointerleave', hide);
        el.addEventListener('focus', () => { const box=el.getBoundingClientRect(); show(box.x + box.width/2, box.y); });
        el.addEventListener('blur', hide);
        el.addEventListener('keydown', event => { if (event.key === 'Escape') hide(); });
    }
    window.addEventListener('scroll', () => { tooltip.hidden = true; }, {passive:true});
    root.querySelectorAll('[data-counter]').forEach(el => {
        const target = num(el.dataset.counter);
        visible(el, () => animate(1100, progress => {
            const value = target * progress;
            el.textContent = el.dataset.format === 'money' ? money(value) : el.dataset.format === 'percent' ? decimal.format(value) + '%' : el.dataset.format === 'decimal' ? decimal.format(value) : whole.format(value);
        }));
    });
    // SVG bars use a zero baseline. Empty datasets are explicitly labeled.
    function barChart(target, values, options) {
        target.replaceChildren();
        tooltip.hidden = true;
        const generation = (num(target.dataset.generation) + 1);
        target.dataset.generation = generation;
        const valid = () => num(target.dataset.generation) === generation;
        const width = options.width, height = options.height;
        const left = 58, right = 15, top = 25, bottom = 42;
        const plotW = width - left - right, plotH = height - top - bottom, baseline = height-bottom;
        const maximum = Math.max(0, ...values.map(item => item.value));
        const magnitude = maximum > 0 ? Math.pow(10, Math.floor(Math.log10(maximum))) : 1;
        let upper = maximum > 0 ? Math.ceil(maximum / magnitude) * magnitude : 1;
        if (options.integer) upper = Math.max(4, Math.ceil(upper/4)*4);
        for (let index=0; index<=4; index++) {
            const y=baseline-plotH*index/4;
            target.appendChild(svg('line', {x1:left,y1:y,x2:width-right,y2:y,stroke:'#ece6dd','stroke-dasharray':'3 6'}));
            target.appendChild(svg('text',{x:left-10,y:y+4,'text-anchor':'end',fill:'#9c9083','font-size':10},compact.format(upper*index/4)));
        }
        const slot=plotW/Math.max(values.length,1), marks=[];
        if (options.area && maximum > 0) {
            // A linear area uses the same monthly amounts; it does not interpolate extra observations.
            const points=values.map((item,index)=>[left+slot*(index+.5), baseline-item.value/upper*plotH]);
            const defs=svg('defs');
            const gradient=svg('linearGradient',{id:'hd-revenue-gradient',x1:'0',y1:'0',x2:'0',y2:'1'});
            gradient.append(svg('stop',{offset:'0%','stop-color':'#b29a7b','stop-opacity':'.42'}),svg('stop',{offset:'100%','stop-color':'#b29a7b','stop-opacity':'.02'}));
            defs.appendChild(gradient); target.appendChild(defs);
            const path='M '+points.map(point=>point.join(' ')).join(' L ');
            const area=svg('path',{d:path+' L '+points[points.length-1][0]+' '+baseline+' L '+points[0][0]+' '+baseline+' Z',fill:'url(#hd-revenue-gradient)',opacity:0});
            const line=svg('path',{d:path,fill:'none',stroke:'#a58a64','stroke-width':2.5,opacity:0});
            target.append(area,line); marks.push({area,line});
            values.forEach((item,index)=>{
                const point=svg('circle',{cx:points[index][0],cy:points[index][1],r:5,fill:'#a58a64',stroke:'#fff','stroke-width':2});
                interactive(point, options.describe(item)); target.appendChild(point);
            });
        }
        values.forEach((item,index)=>{
            const x=left+slot*(index+.5), h=item.value/upper*plotH;
            if (!options.area) {
                const bar=svg('rect',{x:x-slot*.25,y:baseline,width:slot*.5,height:0,rx:4,fill:item.color || '#b29a7b'});
                if (item.value > 0) { interactive(bar,options.describe(item)); target.appendChild(bar); marks.push({bar,h}); }
                const text=svg('text',{x,y:baseline-h-9,'text-anchor':'middle',fill:'#887661','font-size':9},compact.format(item.value));
                target.appendChild(text);
            }
            target.appendChild(svg('text',{x,y:baseline+23,'text-anchor':'middle',fill:'#8a7d6e','font-size':10},item.axis));
        });
        if (maximum === 0) target.appendChild(svg('text',{x:left+plotW/2,y:top+plotH/2,'text-anchor':'middle',fill:'#9c9083','font-size':12},options.empty));
        animate(1000,progress=>marks.forEach(mark=>{
            if(mark.bar) { mark.bar.setAttribute('height',mark.h*progress); mark.bar.setAttribute('y',baseline-mark.h*progress); }
            else { mark.area.setAttribute('opacity',progress); mark.line.setAttribute('opacity',progress); }
        }),valid);
    }
    const months=read('hd-month-data');
    const revenue=document.getElementById('hd-revenue-chart');
    let revenueMode='bar';
    function drawRevenue() {
        barChart(revenue,months.map(item=>({...item,axis:'T'+item.label})),{width:800,height:310,area:revenueMode==='area',describe:item=>'Month '+item.label+'\nPaid revenue: '+money(item.value),empty:'No paid revenue recorded for this year'});
    }
    if (revenue) visible(revenue,drawRevenue);
    document.querySelectorAll('[data-revenue-mode]').forEach(button=>button.addEventListener('click',()=>{
        revenueMode=button.dataset.revenueMode;
        document.querySelectorAll('[data-revenue-mode]').forEach(other=>other.setAttribute('aria-pressed',String(other===button)));
        drawRevenue();
    }));
    function donut(target,legend,data,noun) {
        const total=data.reduce((sum,item)=>sum+item.value,0), radius=83, circumference=2*Math.PI*radius;
        target.appendChild(svg('circle',{cx:115,cy:115,r:radius,fill:'none',stroke:'#f1ece4','stroke-width':25}));
        const arcs=[]; let offset=0;
        data.forEach(item=>{
            const ratio=total>0?item.value/total:0, length=ratio*circumference;
            if(length>0) {
                const arc=svg('circle',{cx:115,cy:115,r:radius,fill:'none',stroke:color(item.label),'stroke-width':25,'stroke-dasharray':'0 '+circumference,'stroke-dashoffset':-offset,transform:'rotate(-90 115 115)'});
                interactive(arc,name(item.label)+'\n'+whole.format(item.value)+' '+noun+' · '+decimal.format(ratio*100)+'%');
                target.appendChild(arc); arcs.push({arc,length});
            }
            offset+=length;
            const row=node('div','hd-legend-row'),swatch=node('span','hd-swatch'); swatch.style.background=color(item.label);
            row.append(swatch,node('span','',name(item.label)),node('strong','',whole.format(item.value)),node('small','',decimal.format(ratio*100)+'%')); legend.appendChild(row);
        });
        visible(target,()=>animate(1250,progress=>arcs.forEach(item=>{const length=item.length*progress;item.arc.setAttribute('stroke-dasharray',length+' '+(circumference-length));})));
        if(total===0) target.setAttribute('aria-label','No '+noun+' to display');
        return total;
    }
    const booking=document.getElementById('hd-booking-chart');
    if(booking) donut(booking,document.getElementById('hd-booking-legend'),read('hd-booking-data'),'bookings');
    const room=document.getElementById('hd-room-chart');
    if(room) { const total=donut(room,document.getElementById('hd-room-legend'),read('hd-room-data'),'rooms'); document.getElementById('hd-room-total').textContent=whole.format(total); }
    const payments=read('hd-payment-data');
    const paymentChart=document.getElementById('hd-payment-chart');
    let paymentMode='amount';
    function drawPayments() {
        const isCount=paymentMode==='count';
        paymentChart.setAttribute('aria-label',isCount?'Transaction counts by payment status':'Payment amounts by status in VND');
        barChart(paymentChart,payments.map(item=>({...item,value:isCount?item.count:item.value,axis:name(item.label),color:color(item.label)})),{width:440,height:280,integer:isCount,describe:item=>name(item.label)+'\n'+(isCount?whole.format(item.value)+' transactions':money(item.value)),empty:isCount?'No dated transactions in this period':'No dated payment amounts in this period'});
    }
    if(paymentChart) {
        visible(paymentChart,drawPayments);
        const summary=document.getElementById('hd-payment-summary');
        payments.forEach(item=>{const cell=node('div','hd-mini-cell');cell.append(node('span','',name(item.label)),node('strong','',money(item.value)),node('small','',whole.format(item.count)+' transactions'));summary.appendChild(cell);});
    }
    document.querySelectorAll('[data-payment-mode]').forEach(button=>button.addEventListener('click',()=>{
        paymentMode=button.dataset.paymentMode;
        document.querySelectorAll('[data-payment-mode]').forEach(other=>other.setAttribute('aria-pressed',String(other===button)));
        drawPayments();
    }));
    const occupancy=document.getElementById('hd-occupancy-ring');
    if(occupancy) {
        const fraction=num(occupancy.dataset.available)>0?Math.min(100,Math.max(0,num(occupancy.dataset.value)))/100:0, circumference=2*Math.PI*39;
        visible(occupancy,()=>animate(1200,progress=>{const length=circumference*fraction*progress;occupancy.setAttribute('stroke-dasharray',length+' '+(circumference-length));}));
    }
    // The rating gauge represents the approved average; no star distribution is invented.
    const rating=document.getElementById('hd-rating');
    if(rating) {
        const value=Math.max(0,Math.min(5,num(rating.dataset.value))), chart=document.getElementById('hd-rating-chart');
        const path='M 35 155 A 115 115 0 0 1 265 155';
        chart.appendChild(svg('path',{d:path,fill:'none',stroke:'#f1ece4','stroke-width':20,'stroke-linecap':'round'}));
        const active=svg('path',{d:path,fill:'none',stroke:'#b29a7b','stroke-width':20,'stroke-linecap':'butt',pathLength:100,'stroke-dasharray':'0 100'});
        chart.appendChild(active);
        chart.append(svg('text',{x:27,y:184,fill:'#9c9083','font-size':11},'0'),svg('text',{x:267,y:184,fill:'#9c9083','font-size':11},'5'));
        const stars=document.getElementById('hd-rating-stars');
        visible(rating,()=>animate(1300,progress=>{const length=value/5*100*progress;active.setAttribute('stroke-dasharray',length+' '+(100-length));stars.style.width=length+'%';}));
    }
})();
