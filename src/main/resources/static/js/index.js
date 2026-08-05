function money(cents){
    return "$" + (cents/100).toLocaleString("en-US",{
       minimumFractionDigits:2,maximumFractionDigits:2
    });
}

function esc(s) {
    return String(s).replace(/[&<>"']/g, c =>
        ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

function parsePanel(panelJson){
    try{
        const p = JSON.parse(panelJson);
        return p && typeof p === "object" ? p : {hue:30,faders:4,knobs:4,keys:false,wood:false};


    }catch{
        return {hue:30,faders: 4,knobs:4,keys: false,wood: false};
    }
}

//decorative
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

document.addEventListener("DOMContentLoaded",loadFloor);

async function loadFloor(){
    const grid = document.getElementById("cardGrid");

    if(!grid){
        console.warn("index.js: #cardGrid not found");
        return;
    }

    try {
        const res=await fetch("/api/products",{credentials:"include"});
        if(!res.ok) throw new Error("HTTP "+res.status);
        const data = await res.json();
        const products = data.items;

        if(products.length===0){
            grid.innerHTML='<p class="empty-note">Nothing on the floor right now.</p>';
            return;
        }

       grid.innerHTML=products.map(cardHTML).join("");
    }catch (err){
        console.error("index.js: failed to load products",err);
        grid.innerHTML='<p class="empty-note">Couldn\'t load the floor. Try refreshing.</p>';
    }

}

function cardHTML(p){
    const panel = parsePanel(p.panelJson);
    const gradeLetter = p.grade || "-";

    return `
    <a class="card" href="product.html?slug=${encodeURIComponent(p.slug)}">
      <div class="card-art">
        ${panelHTML(panel, p.maker + " " + p.name)}
      </div>
      <div class="card-body">
        <div class="card-head">
          <span class="card-maker mono">${esc(p.maker)}</span>
          <span class="grade grade-${gradeLetter.toLowerCase()}">${esc(gradeLetter)}</span>
        </div>
        <h3 class="card-name">${esc(p.name)}</h3>
        <div class="card-foot">
          <span class="card-serial mono">${esc(p.serialNumber)}</span>
          <span class="card-price mono">${money(p.priceCents)}</span>
        </div>
      </div>
    </a>`;
}