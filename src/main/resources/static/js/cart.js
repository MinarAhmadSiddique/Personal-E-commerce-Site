function money(cents){
    return "$" +(cents/100).toLocaleString("en-US",{
       minimumFractionDigits:2,maximumFractionDigits:2
    });
}

function esc(s) {
    return String(s).replace(/[&<>"']/g, c =>
        ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

const SHIPPING_CENTS = 2500;

function  readCart(){
    try{
        const v =JSON.parse(localStorage.getItem("sn.cart"));
        return Array.isArray(v) ? v : [];
    }catch{
        return [];
    }
}

function writeCart(list) {localStorage.setItem("sn.cart",JSON.stringify(list));}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", loadCart);
} else {
    loadCart();
}

async function loadCart() {
    const slugs = readCart();

    const cartGrid = document.getElementById("cartGrid");
    const emptyCart = document.getElementById("emptyCart");

    if (slugs.length === 0) {
        if (cartGrid) cartGrid.setAttribute("hidden", "");
        if (emptyCart) emptyCart.removeAttribute("hidden");
        return;
    }

    // NON-empty: make sure the cart grid is visible and the empty state is hidden
    if (cartGrid) cartGrid.removeAttribute("hidden");
    if (emptyCart) emptyCart.setAttribute("hidden", "");

    const results = await Promise.allSettled(
        slugs.map(slug =>
            fetch("/api/products/" + encodeURIComponent(slug), { credentials: "include" })
                .then(res => {
                    if (res.status === 404) return { slug, closed: true };
                    if (!res.ok) throw new Error("HTTP " + res.status);
                    return res.json();
                })
        )
    );

    const lines = results.map((r, i) =>
        r.status === "fulfilled" ? r.value : { slug: slugs[i], closed: true }
    );

    renderItems(lines);
    renderSummary(lines);
}

function renderItems(lines){
    const box = document.getElementById("itemsBox");
    if(!box) return;

    box.innerHTML = lines.map(line => {
        if(line.closed){
            return `
        <div class="cart-line cart-line-closed" data-slug="${esc(line.slug)}">
          <div class="cart-line-body">
            <span class="cart-line-name">This listing has closed</span>
            <span class="cart-line-note mono">Sold while it sat in your cart.</span>
          </div>
          <button class="row-btn" data-remove="${esc(line.slug)}">Remove</button>
        </div>`;
        }
        return `
      <div class="cart-line" data-slug="${esc(line.slug)}">
        <div class="cart-line-body">
          <span class="cart-line-maker mono">${esc(line.maker)}</span>
          <span class="cart-line-name">${esc(line.name)}</span>
          <span class="cart-line-serial mono">${esc(line.serialNumber)}</span>
        </div>
        <span class="cart-line-price mono">${money(line.priceCents)}</span>
        <button class="row-btn" data-remove="${esc(line.slug)}">Remove</button>
      </div>`;
    }).join("");

    box.addEventListener("click",(e)=>{
       const btn = e.target.closest("[data-remove]");
       if(!btn) return;
       const slug = btn.dataset.remove;
       writeCart(readCart().filter(s=>s!==slug));
       loadCart();
    });
}

function renderSummary(lines) {
    const box = document.getElementById("summaryBox");
    const checkoutBtn = document.getElementById("checkoutBtn");
    if (!box) return;

    const live = lines.filter(l => !l.closed);
    const subtotal = live.reduce((sum, l) => sum + l.priceCents, 0);
    const hasLive = live.length > 0;

    box.innerHTML = `
    <dl class="spec-sheet">
      <div class="spec-row"><dt class="mono">Units</dt><dd class="mono">${live.length}</dd></div>
      <div class="spec-row"><dt class="mono">Subtotal</dt><dd class="mono">${money(subtotal)}</dd></div>
      <div class="spec-row"><dt class="mono">Shipping</dt><dd class="mono">${money(hasLive ? SHIPPING_CENTS : 0)}</dd></div>
      <div class="spec-row spec-row-lead"><dt class="mono">Total</dt>
        <dd class="mono">${money(subtotal + (hasLive ? SHIPPING_CENTS : 0))}</dd></div>
    </dl>
    <p class="summary-note">The cart doesn't hold a unit — stock is claimed at checkout.</p>`;

    // Checkout is disabled when there's nothing live to buy.
    if (checkoutBtn) {
        if (hasLive) {
            checkoutBtn.removeAttribute("aria-disabled");
            checkoutBtn.classList.remove("is-disabled");
            checkoutBtn.href = "checkout.html";
        } else {
            checkoutBtn.setAttribute("aria-disabled", "true");
            checkoutBtn.classList.add("is-disabled");
            checkoutBtn.removeAttribute("href");
        }
    }
}