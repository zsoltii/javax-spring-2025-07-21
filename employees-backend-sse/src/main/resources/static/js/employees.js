window.onload = function () {
    const source = new EventSource("/api/employees/messages")
    source.addEventListener("EmployeeDto", function (event) {
        const data = event.data
        const json = JSON.parse(data)
        const name = json.name

        console.log(`Received message: ${name}`)

        document.querySelector("#messages-div").innerHTML += `<p>${name}</p>`
    })
}