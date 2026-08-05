function money(cents){
    return "$" +(cents/100).toLocaleString("en-us",{
        minimumFractionDigits:2,maximumFractionDigits:2
    });
}

function esc(s) {
    return String(s).replace(/[&<>"']/g, c =>
        ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

const SHIPPING_CENTS=2500;

function readCart(){
    try{
        const v =JSON.parse(localStorage.getItem("sn.cart"));
        return Array.isArray(v) ? v:[];
    }catch {return [];}
}

function writeCart(list){
    localStorage.setItem("sn.cart",JSON.stringify(list));
}

if(document.readyState === "loading"){
    document.addEventListener("DOMContentLoaded",boot);
}else{
    boot();
}

async function boot(){
    try{
        const me = await  fetch("/api/auth/me",{credentials:"include"});
        if(me.status===401){
            location.href="../login.html?next=../checkout.html";
            return;
        }
    }catch {
        location.href ="../login.html?next=../checkout.html";
        return;
    }

    await loadSummary();

    const form = document.getElementById("checkoutForm");
    if(form) form.addEventListener("submit",onsubmit);
}

let liveLines = [];

async function loadSummary(){
    const slugs=readCart();
    const view = document.getElementById("checkoutView");
    const empty = document.getElementById("emptyCheckout");

    if(slugs.length === 0){
        if(view) view.setAttribute("hidden");
        if(empty) empty.removeAttribute("hidden");
        return;
    }

    if(view) view.removeAttribute("hidden");
    if(empty) empty.setAttribute("hidden","");

    const results = await Promise.allSettled(
      slugs.map(slug=>
      fetch("/api/products/"+encodeURIComponent(slug),{credentials:"include"})
          .then(res=>{
              if(res.status === 404) return {slug,closed: true};
              if(!res.ok) throw new Error("HTTP"+res.status);
              return res.json();
          })
      )
    );
    const lines = results.map((r,i)=>
    r.status==="fulfilled" ? r.value:{slug: slugs[i],closed: true});
    liveLines = lines.filter(l=>!l.closed);

    renderItems(lines);
    renderTotals();
}

function renderItems(lines){
    const box = document.getElementById("coItems");
    if(!box) return;

    box.innerHTML = lines.map(l=>l.closed
        ? `<div class="cart-line cart-line-closed"><span class="cart-line-name">Listing closed — removed from order</span></div>`
        : `<div class="cart-line">
         <div class="cart-line-body">
           <span class="cart-line-maker mono">${esc(l.maker)}</span>
           <span class="cart-line-name">${esc(l.name)}</span>
         </div>
         <span class="cart-line-price mono">${money(l.priceCents)}</span>
       </div>`
    ).join("");
}

function renderTotals(){
    const box = document.getElementById("coTotals");
    if(!box) return;
    const subtotal = liveLines.reduce((s,l) => s+l.priceCents,0);
    const hasLive = liveLines.length >0;
    const total= subtotal + (hasLive ? SHIPPING_CENTS :0);
    box.innerHTML = `
    <dl class="spec-sheet">
      <div class="spec-row"><dt class="mono">Units</dt><dd class="mono">${liveLines.length}</dd></div>
      <div class="spec-row"><dt class="mono">Subtotal</dt><dd class="mono">${money(subtotal)}</dd></div>
      <div class="spec-row"><dt class="mono">Shipping</dt><dd class="mono">${money(hasLive ? SHIPPING_CENTS : 0)}</dd></div>
      <div class="spec-row spec-row-lead"><dt class="mono">Total</dt><dd class="mono">${money(total)}</dd></div>
    </dl>`;
}

function onSubmit(e){
    e.preventDefault();
    clearErrors();

    if(liveLines.length===0){
        note("There's nothing available to order.","error");
        return;
    }

    const v = collectAndValidate();
    if (!v.ok) return;   // field errors already shown

    const btn = document.getElementById("submitBtn");
    if (btn) { btn.disabled = true; btn.textContent = "Placing order..."; }

    // ---- THE SEAM: today this fakes success; later it POSTs to /api/checkout ----
    placeOrder(v.data)
        .then(showReceipt)
        .catch(() => {
            note("Something went wrong placing the order.", "error");
            if (btn) { btn.disabled = false; btn.textContent = "Place order"; }
        });
}

function showReceipt(order) {
    writeCart([]);   // order "placed" -> empty the cart

    document.getElementById("checkoutView")?.setAttribute("hidden", "");
    const receipt = document.getElementById("receipt");
    if (receipt) receipt.removeAttribute("hidden");

    const idEl = document.getElementById("receiptId");
    if (idEl) idEl.textContent = "Order " + order.id;
    const meta = document.getElementById("receiptMeta");
    if (meta) meta.textContent = `${order.units} unit(s) · ${money(order.total)} · confirmation sent`;
    const note = document.getElementById("receiptNote");
    if (note) note.textContent = "This is a simulated order — no payment was taken and stock wasn't changed.";
    const link = document.getElementById("receiptLink");
    if (link) { link.href = "index.html"; link.textContent = "Back to the floor"; }
}
function collectAndValidate() {
    const fields = {
        name: val("name"), line1: val("line1"), city: val("city"),
        state: val("state"), zip: val("zip"),
        card: val("card"), exp: val("exp"), cvc: val("cvc")
    };
    let ok = true;
    const need = (id, cond, msg) => { if (!cond) { fieldError(id, msg); ok = false; } };

    need("name", fields.name.length > 0, "Enter a name.");
    need("line1", fields.line1.length > 0, "Enter a street address.");
    need("city", fields.city.length > 0, "Enter a city.");
    need("state", /^[A-Za-z]{2}$/.test(fields.state), "Two-letter state.");
    need("zip", /^\d{5}$/.test(fields.zip), "Five-digit ZIP.");
    need("card", fields.card.replace(/\s/g, "").length >= 12, "Enter a card number.");
    need("exp", /^\d{2}\/\d{2}$/.test(fields.exp), "MM/YY.");
    need("cvc", /^\d{3,4}$/.test(fields.cvc), "3–4 digits.");

    return { ok, data: fields };
}

function val(id) { const el = document.getElementById(id); return el ? el.value.trim() : ""; }
function fieldError(id, msg) {
    const p = document.getElementById(id + "Error");
    if (p) { p.textContent = msg; p.removeAttribute("hidden"); }
}
function clearErrors() {
    ["name","line1","city","state","zip","card","exp","cvc"].forEach(id => {
        const p = document.getElementById(id + "Error");
        if (p) { p.textContent = ""; p.setAttribute("hidden", ""); }
    });
    note("", "");
}
function note(msg, kind) {
    const el = document.getElementById("formNote");
    if (!el) return;
    if (!msg) { el.setAttribute("hidden", ""); return; }
    el.textContent = msg;
    el.className = "form-note form-note-" + kind;
    el.removeAttribute("hidden");
}