window.onload = function () {
    const socket = new SockJS("/websocket-endpoint")
    const client = Stomp.over(socket)

    document.querySelector("#message-button").onclick = function () {
        const text = document.querySelector("#message-input").value
        const json = JSON.stringify({"requestText": text})
        client.send("/app/messages", {}, json)
    }

    client.connect({}, function (frame) {
        client.subscribe("/topic/employees", function (message) {
            const body = message.body
            const json = JSON.parse(body)
            const text = json.responseText

            console.log(`Reply: ${text}`)
            document.querySelector("#message-div").innerHTML += `<p>${text}</p>`
        })
    })
}