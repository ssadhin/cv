const fs = require('fs');
const jsdom = require('jsdom');
const { JSDOM } = jsdom;

const html = fs.readFileSync('c:\\Users\\hasad\\StudioProjects\\MyApplicatio\\app\\src\\main\\assets\\index.html', 'utf8');

const dom = new JSDOM(html);
const window = dom.window;
const document = window.document;

// Mock Android
window.Android = {
    updateNativeDesignSelection: () => { }
};

// Create a section
const sec = document.createElement('section');
sec.dataset.type = 'experience';
// Add Default Modern html for experience
sec.innerHTML = `<div class="data-table-item"><div class="table-row" style="justify-content:space-between"><span class="table-val" contenteditable="true" style="font-weight:700">Company</span><span class="table-val" contenteditable="true" style="font-size:0.85em; color: var(--text-muted);">Date - Date</span></div><div class="table-row" style="margin-top:-2px; margin-bottom:5px; font-size:0.85em; color:#777;"><i class="fas fa-map-marker-alt" style="font-size:0.9em; margin-right:4px;"></i><span class="table-val" contenteditable="true">Location</span></div><div class="table-row"><span class="table-val exp-role" contenteditable="true">Role</span></div><ul class="resp-list"><li contenteditable="true">Responsibility...</li></ul></div>`;
document.body.appendChild(sec);

// Find the applySectionDesign function
let applySectionDesignCode = '';
const scriptTags = document.querySelectorAll('script');
scriptTags.forEach(s => {
    if (s.textContent.includes('function applySectionDesign(')) {
        applySectionDesignCode = s.textContent;
    }
});

if (applySectionDesignCode) {
    // Extract everything needed
    // Actually, just extract the relevant block for bento!
    const items = sec.querySelectorAll('.data-table-item');
    items.forEach(item => {
        // 1. Create Bento containers
        const hero = document.createElement('div'); hero.className = 'bento-hero';
        const meta = document.createElement('div'); meta.className = 'bento-meta';
        const dateBox = document.createElement('div'); dateBox.className = 'bento-date-box';
        const content = document.createElement('div'); content.className = 'bento-content';

        // 2. Identify elements
        const rows = Array.from(item.querySelectorAll('.table-row, .pd-row'));
        const role = item.querySelector('.exp-role');
        const list = item.querySelector('.resp-list, .proj-desc');

        // Extract Date (Robust matching)
        let dateText = "Period";
        let dEl = null;

        // Try finding by labels first (matches timeline logic)
        const labels = Array.from(item.querySelectorAll('.table-label'));
        const dateLabel = labels.find(el => {
            const t = el.textContent.toLowerCase();
            return t.includes('year') || t.includes('date') || t.includes('duration') || t.includes('period') || t.includes('time');
        });

        if (dateLabel) {
            dEl = dateLabel.parentElement.querySelector('.table-val');
            if (dEl) {
                dateText = dEl.textContent.trim();
                const otherLabels = Array.from(dateLabel.parentElement.querySelectorAll('.table-label')).filter(l => l !== dateLabel);
                if (otherLabels.length > 0) {
                    dateLabel.classList.add('timeline-extracted');
                    dEl.classList.add('timeline-extracted');
                } else {
                    dateLabel.parentElement.classList.add('timeline-extracted');
                }
            }
        } else {
            // Fallback to muted values
            const mutedVals = Array.from(item.querySelectorAll('.table-val')).filter(el => {
                const s = el.getAttribute('style') || '';
                return s.includes('text-muted') || s.includes('color: #777') || s.includes('#666');
            });
            if (mutedVals.length > 0) {
                dEl = mutedVals[mutedVals.length - 1];
                dateText = dEl.textContent;
                dEl.classList.add('timeline-extracted');
            }
        }

        // 3. Move items into containers
        if (role) hero.appendChild(role);
        else if (rows[0] && rows[0].querySelector('.table-val')) {
            hero.appendChild(rows[0].querySelector('.table-val'));
        }

        dateBox.innerHTML = '<i class="fas fa-calendar-alt"></i>';
        const dateSpan = document.createElement('span');
        dateSpan.textContent = dateText;
        dateBox.appendChild(dateSpan);

        rows.forEach(row => {
            // Let's use the dashboard.html condition!
            if (!row.contains(role) && !row.classList.contains('timeline-extracted') && !row.querySelector('.timeline-extracted')) {
                meta.appendChild(row); // This expects row to NOT have .timeline-extracted inside it
            }
        });

        if (list) content.appendChild(list);

        // 4. Assemble
        item.appendChild(hero);
        item.appendChild(meta);
        item.appendChild(dateBox);
        item.appendChild(content);
    });

    console.log("FINAL HTML:\n", sec.innerHTML);
}
