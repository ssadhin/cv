
        window.logStyleDebug = function(msg) {
            const logDiv = document.getElementById('styleDebugLog');
            if (logDiv) {
                const time = new Date().toISOString().split('T')[1].substring(0,8);
                logDiv.innerHTML += `[${time}] ${msg}\n`;
                logDiv.scrollTop = logDiv.scrollHeight;
            }
            console.log("[StyleDebug] " + msg);
        };
        window.dumpExpHtml = function(context) {
            try {
                const exp = document.querySelector('section[data-type="experience"]');
                if (exp) {
                    const cloned = exp.cloneNode(true);
                    // remove SVG icons to save log space
                    cloned.querySelectorAll('i').forEach(i => i.remove());
                    let html = cloned.innerHTML.replace(/\s+/g, ' ').trim();
                    if (html.length > 150) html = html.substring(0, 150) + "...";
                    window.logStyleDebug(context + " | Exp HTML: " + html);
                } else {
                    window.logStyleDebug(context + " | Exp HTML: NOT FOUND");
                }
            } catch(e) {
                window.logStyleDebug(context + " | Exp HTML: Error " + e.message);
            }
        };
        function toggleStyleDebug() {
            const logDiv = document.getElementById('styleDebugLog');
            if (logDiv.style.display === 'none') {
                logDiv.style.display = 'block';
                document.getElementById('styleDebugPanel').style.width = '350px';
            } else {
                logDiv.style.display = 'none';
                document.getElementById('styleDebugPanel').style.width = '120px';
            }
        }
        function copyStyleDebug() {
            const text = document.getElementById('styleDebugLog').innerText;
            if (window.Android && window.Android.copyToClipboard) {
                window.Android.copyToClipboard(text);
                window.logStyleDebug("Copied to clipboard via Android");
            } else {
                navigator.clipboard.writeText(text).then(() => window.logStyleDebug("Copied")).catch(e => window.logStyleDebug("Copy failed"));
            }
        }
        setTimeout(() => window.logStyleDebug("Debug Panel Initialized"), 500);

        // ── Visual Tentacle System ──────────────────────────────────
        function drawTentacle(sourceId, targetId, pathId) {
            const source = document.getElementById(sourceId);
            const target = document.getElementById(targetId);
            const overlay = document.getElementById('tentacleOverlay');
            if (!source || !target || !overlay) return;

            const sourceRect = source.getBoundingClientRect();
            const targetRect = target.getBoundingClientRect();
            const overlayRect = overlay.getBoundingClientRect();

            // Source center point
            const sx = sourceRect.left + sourceRect.width / 2 - overlayRect.left;
            const sy = sourceRect.top + sourceRect.height / 2 - overlayRect.top;

            // Target anchor point
            let tx, ty;
            if (targetId === 'mainHeader') {
                // Header anchor: bottom center
                tx = targetRect.left + targetRect.width / 2 - overlayRect.left;
                ty = targetRect.bottom - overlayRect.top;
            } else if (targetId === 'leftCol' || targetId === 'sidebar') {
                // Sidebar anchor: right edge center
                const col = document.getElementById('leftCol') || document.querySelector('.left-column');
                if (!col) return;
                const colRect = col.getBoundingClientRect();
                tx = colRect.right - overlayRect.left;
                ty = colRect.top + colRect.height / 2 - overlayRect.top;
            } else {
                tx = targetRect.left - overlayRect.left;
                ty = targetRect.top + targetRect.height / 2 - overlayRect.top;
            }

            // Curvature logic
            const cp1x = sx + (tx - sx) / 3;
            const cp1y = sy;
            const cp2x = sx + (tx - sx) / 1.5;
            const cp2y = ty;

            const d = `M ${sx} ${sy} C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${tx} ${ty}`;

            let path = document.getElementById(pathId);
            if (!path) {
                path = document.createElementNS("http://www.w3.org/2000/svg", "path");
                path.id = pathId;
                path.setAttribute("class", "tentacle-path active-tentacle");
                overlay.appendChild(path);
            }
            path.setAttribute("d", d);
        }

        function clearTentacle(pathId) {
            const path = document.getElementById(pathId);
            if (path) path.remove();
        }

        function updateAllTentacles() {
            const hToggle = document.getElementById('headerHeightToggle');
            const sToggle = document.getElementById('sidebarWidthToggle');
            const panel = document.getElementById('sidePanel');
            
            if (panel && !panel.classList.contains('open')) {
                const overlay = document.getElementById('tentacleOverlay');
                if (overlay) overlay.innerHTML = '';
                return;
            }

            if (hToggle && hToggle.checked) {
                drawTentacle('headerHeightToggle', 'mainHeader', 'header-tentacle');
            } else {
                clearTentacle('header-tentacle');
            }

            if (sToggle && sToggle.checked) {
                drawTentacle('sidebarWidthToggle', 'leftCol', 'sidebar-tentacle');
            } else {
                clearTentacle('sidebar-tentacle');
            }
        }

        // Hook into animation loop for smoothness during scrolls/resizes
        let tentacleLoopId = null;
        function tentacleLoop() {
            const panel = document.getElementById('sidePanel');
            if (panel && panel.classList.contains('open')) {
                updateAllTentacles();
                tentacleLoopId = requestAnimationFrame(tentacleLoop);
            } else {
                cancelAnimationFrame(tentacleLoopId);
            }
        }

        // Global watcher for panel toggle
        const observer = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                if (mutation.attributeName === 'class' && mutation.target.id === 'sidePanel') {
                    if (mutation.target.classList.contains('open')) {
                        tentacleLoop();
                    } else {
                        const overlay = document.getElementById('tentacleOverlay');
                        if (overlay) overlay.innerHTML = '';
                    }
                }
            });
        });
        
        const sidePanel = document.getElementById('sidePanel');
        if (sidePanel) {
            observer.observe(sidePanel, { attributes: true });
        }

        // Event Listeners for Toggles - ensure loop starts if user check it
        document.addEventListener('change', (e) => {
            if (e.target.classList.contains('tentacle-checkbox')) {
                if (e.target.checked) {
                    tentacleLoop();
                }
            }
        });
        // ─────────────────────────────────────────────────────────────
    