fetch("http://localhost:8081/api/employees")
.then(response => response.json())
.then(data => {
    const employeesDiv = document.getElementById("employees");
    for (const employee of data) {
        employeesDiv.innerHTML += `<div>${employee.name}</div>`
    }
})