const fs = require('fs');
let code = fs.readFileSync('app/src/main/assets/index.html', 'utf8');

const regex = /if\s*\([^)]*\)\s*\{\s*window\.updateSectionHeaderStyle\([^)]*\);\s*if\s*\([^\{]*\{\s*var\s*props\s*=\s*window\._scrapeSectionCustomHeaderProps\([^)]*\);\s*if\s*\([^)]*\)\s*window\.updateSectionHeaderCustomStyle\([^)]*\);\s*\}\s*\}/;

const newBlock = `if (options.style) {
                        const sourceHeader = sourceSec.querySelector('.section-header, h2');
                        const targetHeader = targetSec.querySelector('.section-header, h2');
                        if (sourceHeader && targetHeader) {
                            if (sourceHeader.classList.contains('custom-header')) {
                                const h2 = sourceHeader;
                                const style = getComputedStyle(h2);
                                const props = {
                                    iconBg: style.getPropertyValue('--h2-icon-bg').trim() || '',
                                    textBg: style.getPropertyValue('--h2-text-bg').trim() || '',
                                    iconRadius: parseInt(style.getPropertyValue('--h2-icon-radius') || '0'),
                                    textRadius: parseInt(style.getPropertyValue('--h2-text-radius') || '0'),
                                    swapIcon: style.flexDirection === 'row-reverse',
                                    connectToggle: h2.classList.contains('connected'),
                                    connectorShape: h2.dataset.connectorShape || '0',
                                    textLeftShape: h2.dataset.textLeftShape || '0',
                                    textRightShape: h2.dataset.textRightShape || '0',
                                    iconShape: h2.dataset.iconShape || '0',
                                    textLeftW: h2.dataset.textLeftW || '10',
                                    textLeftH: h2.dataset.textLeftH || '0',
                                    textLeftColor: h2.dataset.textLeftColor || '',
                                    textRightW: h2.dataset.textRightW || '10',
                                    textRightH: h2.dataset.textRightH || '0',
                                    textRightColor: h2.dataset.textRightColor || '',
                                    connectorW: h2.querySelector('.h2-connector') ? h2.querySelector('.h2-connector').style.width.replace('px', '') : '',
                                    connectorH: h2.querySelector('.h2-connector') ? h2.querySelector('.h2-connector').style.height.replace('px', '') : '',
                                    connectorColor: h2.querySelector('.h2-connector') ? h2.querySelector('.h2-connector').style.background : '',
                                    titleFontColor: h2.querySelector('.h2-text-wrap') ? getComputedStyle(h2.querySelector('.h2-text-wrap')).color : style.color,
                                    iconSize: h2.querySelector('i') ? parseInt(getComputedStyle(h2.querySelector('i')).fontSize) : 24,
                                    titleFontSize: h2.querySelector('.h2-text-wrap') ? parseInt(getComputedStyle(h2.querySelector('.h2-text-wrap')).fontSize) : parseInt(style.fontSize),
                                    iconFrameWidth: h2.dataset.iconFrameWidth || '0',
                                    iconFrameColor: h2.dataset.iconFrameColor || '',
                                    textFrameWidth: h2.dataset.textFrameWidth || '0',
                                    textFrameColor: h2.dataset.textFrameColor || ''
                                };
                                window.applySavedCustomStyle(targetSec.id, JSON.stringify(props));
                            } else if (sourceHeader.classList.contains('hidden-line')) {
                                window.updateSectionHeaderStyle(targetSec.id, 'Hidden Line');
                            } else {
                                window.updateSectionHeaderStyle(targetSec.id, 'Default');
                            }
                        }
                    }`;

if (regex.test(code)) {
    code = code.replace(regex, newBlock);
    fs.writeFileSync('app/src/main/assets/index.html', code, 'utf8');
    console.log('SUCCESS: Restored Custom Header propagation logic');
} else {
    console.log('ERROR: Regex failed to match the target block');
}
