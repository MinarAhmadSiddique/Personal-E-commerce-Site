const form =document.getElementById("loginForm");
const formNote=document.getElementById("formNote");

function setFormNote(message,kind){
    if(!message) {formNote.setAttribute("hidden","");return;}
    formNote.textContent=message;
    formNote.className="form-note form-note-"+kind;
    formNote.removeAttribute("hidden");
}

function safeNext() {
    const raw = new URLSearchParams(location.search).get("next");
    if (raw && /^[\w\-]+\.html(\?[^#]*)?$/.test(raw)) return raw;
    return null;
}

form.addEventListener("submit",async (e) =>{
    e.preventDefault();
    setFormNote(null);

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    const submitBtn = document.getElementById("submitBtn");
    submitBtn.disabled=true;
    submitBtn.textContent="Signing in...";

    try{
        const res=await fetch("/api/auth/signin",{
            method:"POST",
            headers:{"Content-Type":"application/json"},
            credentials:"include",
            body: JSON.stringify({email,password})
        });

        if(res.status===200){
           const user = await res.json();

           const dest = user.role === "ADMIN"?"admin/index.html":(safeNext() || "index.html");
           location.href = dest;
           return;
        }

        if(res.status===401){
            setFormNote("Email or password is incorrect.","error");
        }else if(res.status===400){
            setFormNote("Enter a valid email and password.","error");
        }else{
            setFormNote("Something went wrong. Please try again.","error");
        }
    }catch (err){
        setFormNote("Couldn't reach the server. Is it running?","error");
    } finally {
        submitBtn.disabled=false;
        submitBtn.textContent="Sign in";
    }
});