function money(cents) {
    return "$" + (cents / 100).toLocaleString("en-US", {
        minimumFractionDigits: 2, maximumFractionDigits: 2
    });
}
function esc(s) {
    return String(s).replace(/[&<>"']/g, c =>
        ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}
function parsePanel(panelJson) {
    try {
        const p = JSON.parse(panelJson);
        return p && typeof p === "object" ? p : { hue: 30, faders: 4, knobs: 4, keys: false, wood: false };
    } catch {
        return { hue: 30, faders: 4, knobs: 4, keys: false, wood: false };
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

const PAGE_SIZE =12;

const state={
    category:"",
    grade:"",
    search:"",
    sort:"featured",
    page:0
};

const SORT_MAP = {
    "featured":   "name,asc",
    "price-asc":  "priceCents,asc",
    "price-desc": "priceCents,desc",
    "year-asc":   "modelYear,asc",
    "year-desc":  "modelYear,desc"
};

if(document.readyState==="loading"){
    document.addEventListener("DOMContentLoaded",boot);
}else{
    boot();
}

function boot(){
    wireControls();
    load();
}

async function load(){
    const grid = document.getElementById("cardGrid");
    const empty = document.getElementById("emptyState");
    const count = document.getElementById("resultCount");

    const params = new URLSearchParams();

    if(state.category) params.set("category",state.category);
    if(state.grade) params.set("grade",state.grade);
    if(state.search) params.set("search",state.search);
    params.set("sort",SORT_MAP[state.sort] || "name,asc");
    params.set("page",state.page);
    params.set("size",PAGE_SIZE);

    try{
        const res = await fetch("/api/products?"+params.toString(),{credentials:"include"});
        if(!res.ok) throw new Error("HTTP"+res.status);
        const data=await res.json();

        if(count) count.textContent=data.totalItems + (data.totalItems===1 ? "result":"results");

        if(data.items.length ===0){
            if (grid) grid.innerHTML = "";
            if (empty) empty.removeAttribute("hidden");
            renderPager(data);
            return;
        }

        if(empty) empty.setAttribute("hidden","");
        if(grid) grid.innerHTML=data.items.map(cardHTML).join("");
        renderPager(data);
    }catch (err){
        console.error("products.js: load failed", err);
        if (grid) grid.innerHTML = `<p class="empty-note">Couldn't load the catalog. Try refreshing.</p>`;
    }
}

function cardHTML(p) {
    const panel = parsePanel(p.panelJson);
    const g = p.grade || "-";
    return `
    <a class="card" href="product.html?slug=${encodeURIComponent(p.slug)}">
      <div class="card-art">${panelHTML(panel, p.maker + " " + p.name)}</div>
      <div class="card-body">
        <div class="card-head">
          <span class="card-maker mono">${esc(p.maker)}</span>
          <span class="grade grade-${g.toLowerCase()}">${esc(g)}</span>
        </div>
        <h3 class="card-name">${esc(p.name)}</h3>
        <div class="card-foot">
          <span class="card-serial mono">${esc(p.serialNumber)}</span>
          <span class="card-price mono">${money(p.priceCents)}</span>
        </div>
      </div>
    </a>`;
}

function renderPager(data) {
    const pager = document.getElementById("pager");
    const info = document.getElementById("pageInfo");
    if (info) info.textContent = data.totalPages > 0
        ? `Page ${data.page + 1} of ${data.totalPages}` : "";
    if (!pager) return;

    if (data.totalPages <= 1) {
        pager.innerHTML = "";
        pager.setAttribute("hidden", "");      // hide when 1 page or less
        return;
    }

    pager.removeAttribute("hidden");         // ← SHOW it when there are pages

    const prevDisabled = data.page <= 0;
    const nextDisabled = data.page + 1 >= data.totalPages;

    pager.innerHTML = `
    <button class="pager-btn" data-page="${data.page - 1}" ${prevDisabled ? "disabled" : ""}>Prev</button>
    <span class="pager-current mono">${data.page + 1} / ${data.totalPages}</span>
    <button class="pager-btn" data-page="${data.page + 1}" ${nextDisabled ? "disabled" : ""}>Next</button>`;
}

function wireControls(){
    const search = document.getElementById("searchInput");

    if(search){
        let t;
        search.addEventListener("input",()=>{
           clearTimeout(t);
           t=setTimeout(()=>{
               state.search=search.value.trim();
               state.page=0;
               load();
           },300)
        });
    }

    const sort = document.getElementById("sortSelect");
    if(sort){
        sort.addEventListener("change",()=>{
           state.sort=sort.value;
           state.page=0;
           load();
        });
    }

    const cats = document.getElementById("categoryTabs");
    if(cats){
        cats.addEventListener("click",(e)=>{
           const btn = e.target.closest("[data-category]");
           if(!btn) return;
           state.category=btn.dataset.category;
           state.page=0;
            [...cats.querySelectorAll("[data-category]")].forEach(b =>
                b.setAttribute("aria-pressed", b === btn ? "true" : "false"));
            load();
        });
    }
    const grades = document.getElementById("gradeFilter");
    if (grades) {
        grades.addEventListener("click", (e) => {
            const btn = e.target.closest("[data-grade]");
            if (!btn) return;
            const g = btn.dataset.grade;
            state.grade = (state.grade === g) ? "" : g;   // toggle off if same
            state.page = 0;
            [...grades.querySelectorAll("[data-grade]")].forEach(b =>
                b.setAttribute("aria-pressed", b.dataset.grade === state.grade ? "true" : "false"));
            load();
        });
    }

    const pager = document.getElementById("pager");
    if(pager){
        pager.addEventListener("click",(e)=>{
           const btn=e.target.closest("[data-page]");
           if(!btn || btn.disabled) return;

           state.page = parseInt(btn.dataset.page,10);
           load();
           window.scrollTo({top:0,behavior:"smooth"})
        });
    }

    const clear=document.getElementById("clearFilters");
    if(clear){
        clear.addEventListener("click",()=>{
           state.category="";
           state.grade="";
           state.search="";
           state.sort="";
           state.page=0;

            if (cats) [...cats.querySelectorAll("[data-category]")].forEach(b =>
                b.setAttribute("aria-pressed", b.dataset.category === "" ? "true" : "false"));
            if (grades) [...grades.querySelectorAll("[data-grade]")].forEach(b =>
                b.setAttribute("aria-pressed", "false"));
            load();
        });
    }

}