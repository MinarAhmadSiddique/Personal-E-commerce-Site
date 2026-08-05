function money(cents) {
    return "$" + (cents / 100).toLocaleString("en-US", {
        minimumFractionDigits: 2, maximumFractionDigits: 2
    });
}

    function esc(s) {
        return String(s).replace(/[&<>"']/g, c =>
            ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
    }


    function parsePanel(panelJson){
        try{
            const p =JSON.parse(panelJson);
            return p && typeof p==="object" ? p : {hue:30,faders:4,knobs:4,keys:false,wood:false};
        }catch {
            return {hue:30,faders:4,knobs:4,keys:false,wood:false};
        }
    }


function panelHTML(p, label) {
    const faders = Array.from({ length: p.faders || 0 }, (_, i) =>
        `<span class="fader" style="--pos:${18 + ((i * 37) % 64)}%"></span>`).join("");
    const knobs = Array.from({ length: p.knobs || 0 }, (_, i) =>
        `<span class="knob" style="--rot:${-120 + ((i * 71) % 240)}deg"></span>`).join("");
    const keys = p.keys
        ? `<div class="keybed">${Array.from({ length: 17 },
            (_, i) => `<span class="key${[1,3,6,8,10].includes(i % 12) ? " key-black" : ""}"></span>`).join("")}</div>`
        : "";
    return `
    <div class="panel${p.wood ? " panel-wood" : ""}" style="--hue:${p.hue}">
      <div class="panel-face">
        <div class="panel-brand mono">${esc(label || "")}</div>
        ${p.faders ? `<div class="faders">${faders}</div>` : ""}
        ${p.knobs ? `<div class="knobs">${knobs}</div>` : ""}
        <div class="panel-lamp" aria-hidden="true"></div>
      </div>
      ${keys}
    </div>`;
}


const GRADE_MEANINGS = {
  A: "Fully functional, serviced, and calibrated. Cosmetically clean.",
  B: "Fully functional with honest cosmetic wear from use.",
  C: "Functional but compromised - noted faults, priced accordingly.",
  D: "Sold for parts or restoration. Not working as-is."
};

function readCart(){
    try{
        const v = JSON.parse(localStorage.getItem("sn.cart"));
        return Array.isArray(v) ? v:[];
    }catch {return [];}
}

function writeCart(list){localStorage.setItem("sn.cart",JSON.stringify(list));}

document.addEventListener("DOMContentLoaded",loadProduct);

async function loadProduct(){
    const slug = new URLSearchParams(location.search).get("slug");

    if(!slug){
        showNotFounf();
        return;
    }

    try{
        const res = await fetch("/api/products/"+encodeURIComponent(slug),{
            credentials: "include"
        });

        if(res.status === 404){
            showNotFound();
            return;
        }
        if(!res.ok) throw new Error("HTTP" + res.status);

        const p = await res.json();
        render(p);
    }catch (err){
        console.error("product.js: failed to load",err);
        showNotFound();
    }
}

function showNotFound(){
    const view = document.getElementById("productView");
    const nf = document.getElementById("notFound");
    if(view) view.setAttribute("hidden","");
    if(nf) nf.removeAttribute("hidden");
}

function render(p){
    document.title = `${p.maker} ${p.name} - Serial Number`;

    const crumb = document.getElementById("crumbList");

    if (crumb) {
        crumb.innerHTML = `
      <li><a href="index.html">Floor</a></li>
      <li><a href="products.html?category=${encodeURIComponent(p.categorySlug)}">${esc(p.category)}</a></li>
      <li aria-current="page">${esc(p.name)}</li>`;
    }

    const art = document.getElementById("plateArt");
    if(art) art.innerHTML = panelHTML(parsePanel(p.panelJson),p.maker+""+ p.name);

    const info = document.getElementById("plateInfo");

    if (info) {
        const grade = p.grade || "—";
        info.innerHTML = `
      <p class="plate-eyebrow mono">${esc(p.maker)} · ${p.year || ""}</p>
      <h1 class="plate-name">${esc(p.name)}</h1>
      <dl class="spec-sheet">
        <div class="spec-row"><dt class="mono">Serial</dt><dd class="mono">${esc(p.serialNumber)}</dd></div>
        <div class="spec-row"><dt class="mono">Maker</dt><dd class="mono">${esc(p.maker)}</dd></div>
        <div class="spec-row"><dt class="mono">Year</dt><dd class="mono">${esc(String(p.year || "—"))}</dd></div>
        <div class="spec-row"><dt class="mono">Grade</dt><dd class="mono">
          <span class="grade grade-${grade.toLowerCase()}">${esc(grade)}</span></dd></div>
        <div class="spec-row spec-row-lead"><dt class="mono">Price</dt>
          <dd class="mono">${money(p.priceCents)}</dd></div>
      </dl>
      ${p.blurb ? `<p class="plate-blurb">${esc(p.blurb)}</p>` : ""}
      <button class="btn btn-block" id="addToCart" data-slug="${esc(p.slug)}">Add to cart</button>`;

        wireAddToCart(p.slug);
    }

const cond = document.getElementById("conditionLine");
    if(cond && p.grade) cond.textContent= GRADE_MEANINGS[p.grade] || "";

    // sections we don't have backend data for yet — hide them cleanly
    hideIfEmpty("benchTitle", "benchList");
    hideIfEmpty("inclTitle", "includesList");

    loadRelated(p);

    document.getElementById("productView")?.removeAttribute("hidden");
}

function wireAddToCart(slug){
    const btn = document.getElementById("addToCart");
    if(!btn) return;

    const inCart = readCart().includes(slug);
    if (inCart) {
        btn.textContent = "In cart";
        btn.disabled = true;
    }

    btn.addEventListener("click",()=>{
       const cart = readCart();
       if(!cart.includes(slug)){
           cart.push(slug);
           writeCart(cart);
       }

       btn.textContent="In cart";
       btn.disabled=true;
    });
}

function hideIfEmpty(titleId, listId) {
    // These sections needed per-unit content that isn't in the DB yet.
    const title = document.getElementById(titleId);
    const list = document.getElementById(listId);
    if (title) title.setAttribute("hidden", "");
    if (list) list.setAttribute("hidden", "");
}

async function loadRelated(current) {
    const grid = document.getElementById("relatedGrid");
    if (!grid) return;

    try {
        const res = await fetch("/api/products?category=" + encodeURIComponent(current.categorySlug), {
            credentials: "include"
        });
        if (!res.ok) return;
        const all = await res.json();

        const others = all.filter(x => x.slug !== current.slug).slice(0, 3);
        if (others.length === 0) {
            document.getElementById("relatedTitle")?.setAttribute("hidden", "");
            return;
        }

        grid.innerHTML = others.map(x => `
      <a class="card" href="product.html?slug=${encodeURIComponent(x.slug)}">
        <div class="card-art">${panelHTML(parsePanel(x.panelJson), x.maker + " " + x.name)}</div>
        <div class="card-body">
          <div class="card-head">
            <span class="card-maker mono">${esc(x.maker)}</span>
            <span class="grade grade-${(x.grade || "").toLowerCase()}">${esc(x.grade || "—")}</span>
          </div>
          <h3 class="card-name">${esc(x.name)}</h3>
          <div class="card-foot">
            <span class="card-serial mono">${esc(x.serialNumber)}</span>
            <span class="card-price mono">${money(x.priceCents)}</span>
          </div>
        </div>
      </a>`).join("");
    } catch (err) {
        console.error("product.js: related failed", err);
    }
}