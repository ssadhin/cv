
    // ═══════ Wizard Debug Logger ═══════
    window._wizardLogs = [];
    window.profTrace = function(label, data) {
        const pp = document.getElementById('profileSection');
        const np = document.getElementById('nameProfessionSection');
        const header = document.getElementById('mainHeader');
        const dpr = window.devicePixelRatio || 1;

        function getInfo(el) {
            if (!el) return "[MISSING]";
            const cs = window.getComputedStyle(el);
            const br = el.getBoundingClientRect();
            const parent = el.parentElement;
            let pInfo = "NO_PARENT";
            if (parent) {
                const ps = window.getComputedStyle(parent);
                pInfo = `${parent.tagName}(${parent.id || parent.className || '.'}) [disp=${ps.display} jc=${ps.justifyContent} ai=${ps.alignItems} dir=${ps.direction}]`;
            }
            return `[EXISTS pos=${cs.position} t=${cs.top} l=${cs.left} r=${cs.right} ml=${cs.marginLeft} mr=${cs.marginRight} dir=${cs.direction}] | VIEWPORT: [vx=${br.left.toFixed(2)} vy=${br.top.toFixed(2)} vw=${br.width.toFixed(2)} vh=${br.height.toFixed(2)}] PAR:${pInfo}`;
        }
        
        const hbr = header ? header.getBoundingClientRect() : {left:0, top:0};
        const hInfo = `HEADER_ORIGIN: [vx=${hbr.left.toFixed(2)} vy=${hbr.top.toFixed(2)}]`;
        const msg = `PROF_TRACE [${label}] (DPR:${dpr}) ${hInfo} | PP: ${getInfo(pp)} | NP: ${getInfo(np)} ${data || ''}`;
        
        if (window.AndroidLayoutTracker) window.AndroidLayoutTracker.log("Antigravity_Diva", msg);
        console.log(msg);
        
        // Also pipe to the Diva Terminal if it exists
        if (window.divaLog) window.divaLog(msg);
    };

    window.divaLog = function(msg) {
        if (window.AndroidLayoutTracker && typeof window.AndroidLayoutTracker.log === 'function') {
            window.AndroidLayoutTracker.log("Antigravity_Diva", msg);
        }
        
        const terminal = document.getElementById('divaLogTerminal');
        if (terminal) {
            const time = new Date().toISOString().split('T')[1].substring(0, 8);
            const line = document.createElement('div');
            line.className = 'diva-log-line';
            
            let tagClass = '';
            if (msg.includes('[Sync]')) tagClass = 'diva-tag-sync';
            if (msg.includes('[Audit]') || msg.includes('[AUDIT]')) tagClass = 'diva-tag-audit';
            if (msg.includes('Error') || msg.includes('ALERT')) tagClass = 'diva-tag-error';
            if (msg.includes('[🔬 HEIGHT TRACE]')) tagClass = 'diva-tag-height';
            
            line.innerHTML = `<span class="diva-log-timestamp">[${time}]</span><span class="diva-log-tag ${tagClass}">${msg}</span>`;
            terminal.appendChild(line);
            
            // Auto-scroll if near bottom
            if (terminal.scrollHeight - terminal.scrollTop - terminal.clientHeight < 50) {
                terminal.scrollTop = terminal.scrollHeight;
            }
        }
        console.log(msg);
    };

    // ═══════ IDENTITY SPY v3: Synchronous Style Interceptor ═══════
    // MutationObserver fires ASYNC and loses the call stack.
    // This interceptor monkey-patches setAttribute to capture the REAL caller.
    (function setupSyncInterceptor() {
        const WATCHED_IDS = ['profileSection', 'nameProfessionSection'];
        const _diva = function(msg) {
            if (window.AndroidLayoutTracker && typeof window.AndroidLayoutTracker.log === 'function') {
                window.AndroidLayoutTracker.log("Antigravity_Diva", msg);
            }
            console.log(msg);
        };

        // Intercept setAttribute('style', ...) — this catches bulk style overwrites
        const origSetAttribute = Element.prototype.setAttribute;
        Element.prototype.setAttribute = function(name, value) {
            if (name === 'style' && WATCHED_IDS.indexOf(this.id) !== -1) {
                const oldStyle = this.getAttribute('style') || '';
                const stack = new Error().stack || 'NO_STACK';
                const frames = stack.split('\n').slice(1, 10).map(function(f) { return f.trim(); }).join(' << ');
                _diva('🔫 SYNC_SPY [' + this.id + '] setAttribute("style") OLD: [' + oldStyle + '] NEW: [' + value + '] STACK: ' + frames);
            }
            return origSetAttribute.call(this, name, value);
        };

        // Intercept style.cssText setter
        const styleDescriptor = Object.getOwnPropertyDescriptor(CSSStyleDeclaration.prototype, 'cssText');
        if (styleDescriptor && styleDescriptor.set) {
            const origCssTextSetter = styleDescriptor.set;
            Object.defineProperty(CSSStyleDeclaration.prototype, 'cssText', {
                set: function(value) {
                    // Walk up to find the owner element
                    const ownerEl = this.__ownerElement || (function(style) {
                        for (let i = 0; i < WATCHED_IDS.length; i++) {
                            const el = document.getElementById(WATCHED_IDS[i]);
                            if (el && el.style === style) return el;
                        }
                        return null;
                    })(this);
                    
                    if (ownerEl && WATCHED_IDS.indexOf(ownerEl.id) !== -1) {
                        const stack = new Error().stack || 'NO_STACK';
                        const frames = stack.split('\n').slice(1, 10).map(function(f) { return f.trim(); }).join(' << ');
                        _diva('🔫 SYNC_SPY [' + ownerEl.id + '] cssText= OLD: [' + this.cssText + '] NEW: [' + value + '] STACK: ' + frames);
                    }
                    return origCssTextSetter.call(this, value);
                },
                get: styleDescriptor.get,
                configurable: true
            });
        }

        _diva('🌐 SYNC_SPY v3: Synchronous setAttribute + cssText interceptor active');
    })();

    // ═══════ IDENTITY SPY v2: Global MutationObserver ═══════
    // Previous version failed because elements don't exist at script parse time.
    // This version watches the ENTIRE document for element creation and style changes.
    (function setupIdentitySpy() {
        const WATCHED_IDS = ['profileSection', 'nameProfessionSection', 'header_box', 'profile_pic', 'profile_picture', 'description', 'displayName', 'displayProfession'];
        const _armed = {}; // Track which elements are already being watched

        const _diva = function(msg) {
            if (window.AndroidLayoutTracker && typeof window.AndroidLayoutTracker.log === 'function') {
                window.AndroidLayoutTracker.log("Antigravity_Diva", msg);
            }
            console.log(msg);
        };

        function watchElement(el, id) {
            if (_armed[id]) return; // Don't double-arm
            _armed[id] = true;

            let lastStyle = el.getAttribute('style') || '';
            let lastClass = el.className || '';
            const birthTime = performance.now().toFixed(2);
            const r = el.getBoundingClientRect();
            const par = el.parentElement;
            const parRect = par ? par.getBoundingClientRect() : { left: 0, top: 0 };
            
            _diva('✅ IDENTITY_SPY v2 armed on: ' + id + ' [T+' + birthTime + 'ms] VIEWPORT: [vx=' + r.left.toFixed(2) + ' vy=' + r.top.toFixed(2) + '] PARENT: ' + (par ? par.tagName + '(' + (par.id || par.className || 'unnamed') + ')' : 'NULL') + ' @ [' + parRect.left.toFixed(2) + ', ' + parRect.top.toFixed(2) + ']');

            const observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(m) {
                    if (m.attributeName === 'style') {
                        const newStyle = el.getAttribute('style') || '';
                        if (newStyle !== lastStyle) {
                            if (el.classList.contains('is-being-dragged')) {
                                // Mute intensive drag logs to keep swap traces clean
                                lastStyle = newStyle;
                            } else {
                                const stack = new Error().stack || 'NO_STACK';
                                const frames = stack.split('\n').slice(1, 8).map(function(f) { return f.trim(); }).join(' << ');
                                _diva('🔴 IDENTITY_SPY [' + id + '] STYLE CHANGED! OLD: [' + lastStyle + '] NEW: [' + newStyle + '] STACK: ' + frames);
                                lastStyle = newStyle;
                            }
                        }
                    }
                    if (m.attributeName === 'class') {
                        const newClass = el.className || '';
                        if (newClass !== lastClass) {
                            _diva('🟡 IDENTITY_SPY [' + id + '] CLASS CHANGED! OLD: [' + lastClass + '] NEW: [' + newClass + ']');
                            lastClass = newClass;
                        }
                    }
                });
            });

            observer.observe(el, { attributes: true, attributeFilter: ['style', 'class'] });
        }

        // Scan for elements and arm spies
        function scanAndArm() {
            WATCHED_IDS.forEach(function(id) {
                const el = document.getElementById(id);
                if (el && !_armed[id]) {
                    watchElement(el, id);
                }
            });
        }

        // GLOBAL OBSERVER: Watch the entire document for additions
        // This catches elements being created at ANY time, by ANY code path
        const globalObserver = new MutationObserver(function(mutations) {
            let needsScan = false;
            mutations.forEach(function(m) {
                if (m.addedNodes.length > 0) {
                    m.addedNodes.forEach(function(node) {
                        if (node.nodeType === 1) { // Element node
                            if (WATCHED_IDS.indexOf(node.id) !== -1) {
                                const rect = node.getBoundingClientRect();
                                _diva('🆕 IDENTITY_SPY: Element CREATED/REPLACED: #' + node.id + ' VIEWPORT_AT_BIRTH: [vx=' + rect.left.toFixed(2) + ' vy=' + rect.top.toFixed(2) + ']');
                                _armed[node.id] = false; // Reset so it re-arms
                                needsScan = true;
                            }
                            // Also check children (for innerHTML replacements)
                            WATCHED_IDS.forEach(function(id) {
                                if (node.querySelector && node.querySelector('#' + id)) {
                                    _diva('🆕 IDENTITY_SPY: Element FOUND inside new subtree: #' + id);
                                    _armed[id] = false;
                                    needsScan = true;
                                }
                            });
                        }
                    });
                }
                // Also detect removals
                if (m.removedNodes.length > 0) {
                    m.removedNodes.forEach(function(node) {
                        if (node.nodeType === 1 && WATCHED_IDS.indexOf(node.id) !== -1) {
                            _diva('💀 IDENTITY_SPY: Element REMOVED: #' + node.id);
                            _armed[node.id] = false;
                        }
                    });
                }
            });
            if (needsScan) {
                setTimeout(scanAndArm, 10);
            }
        });

        // Start global observer on document.documentElement (catches everything)
        globalObserver.observe(document.documentElement, { childList: true, subtree: true });
        _diva('🌐 IDENTITY_SPY v2: Global observer active, watching for element creation');

        // Initial scan
        scanAndArm();

        // Periodic fallback scan every 2 seconds for the first 30 seconds
        let scanCount = 0;
        const scanInterval = setInterval(function() {
            scanAndArm();
            scanCount++;
            if (scanCount >= 15) clearInterval(scanInterval);
        }, 2000);
    })();


    window.wizardLog = function(tag, msg, data) {
        console.log('[WizardLog][' + tag + '] ' + msg, data || '');
        if (window.divaLog) window.divaLog('[Handoff][' + tag + '] ' + msg);
    };
    
    wizardLog('INFO', 'Wizard log redirected to Diva panel.');
