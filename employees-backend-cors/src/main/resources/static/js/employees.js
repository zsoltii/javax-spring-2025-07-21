fetch("http://localhost:8081/api/employees")
.then(response => response.json())
.then(data => {
    const list = document.getElementById("employees-ul");
    for (const employee of data) {
        list.innerHTML += `<li>${employee.name}</li>`;    
    }
});
