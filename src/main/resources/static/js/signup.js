
    const form =document.getElementById("signupForm");
    const formNote = document.getElementById("formNote");

    function setFormNote(message,kind){
    if(!message) {formNote.setAttribute("hidden","");return;}
    formNote.textContent=message;
    formNote.className = "form-note form-note-" +kind;
    formNote.removeAttribute("hidden");
}

    form.addEventListener("submit",async (e)=>{
    e.preventDefault();
    setFormNote(null);

    const email = document.getElementById("email").value.trim();
    const password=document.getElementById("password").value;
    const confirm = document.getElementById("confirmPassword").value;
    const name = document.getElementById("name").value.trim();

    if(password !== confirm){
    setFormNote("Password don't match.","error");
    return;
}

    if(password.length < 8){
    setFormNote("Password must be at least 8 characters.","error");
    return;
}

    const submitBtn = document.getElementById("submitBtn");
    submitBtn.disabled=true;
    submitBtn.textContent = "Creating account...";

    try{
    const res=await fetch("/api/auth/signup",{
    method:"POST",
    headers:{"Content-Type":"application/json"},
    credentials: "include",
    body: JSON.stringify({email,password,name})
});

    if(res.status === 201){
    const user = await res.json();
    setFormNote(`Account created for ${user.email}. Redirecting to sign in...`,"ok");
    setTimeout(()=>{location.href="login.html"},1200);
    return;
}

    if(res.status === 409){
    setFormNote("That email is already registered. Try signing in.","error");
}else if(res.status === 400){
    setFormNote("Check your details - the server rejected the form.","error");
}else{
    setFormNote("Something went wrong. Please try again.","error");
}
}catch(err){
    setFormNote("Couldn't reach the server. Is it running?","error");
}finally {
    submitBtn.disabled=false;
    submitBtn.textContent="Create account";
}
});
