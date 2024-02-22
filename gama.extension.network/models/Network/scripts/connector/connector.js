// Code fully programmed by Léon Sillano - IRD/ACROSS Lab - 2023/2024

// Default values
const DEFAULT_GAMA_WS_PORT = "1000"
const DEFAULT_GAMA_IP_ADDRESS = "localhost"

// Connection state
const CONNECTED = "CONNECTED"
const DISCONNECTED = "DISCONNECTED"

// Gama Server State
const UNKNOWN = "UNKNOWN"
const NONE = "NONE"
const NOTREADY = "NOTREADY"
const PAUSED = "PAUSED"
const RUNNING = "RUNNING"

//Request or response type
const REQUEST = "REQUEST"
const RESPONSE = "RESPONSE"

// Global variables
var gama_ws_port = DEFAULT_GAMA_WS_PORT
var gama_ip_address = DEFAULT_GAMA_IP_ADDRESS

var connection_state = DISCONNECTED
var gama_state
var model_file 
var experiment_name 
var experiment_id
var current_expression
var current_nb_step
var current_nb_step_back

var socket = createWebSocketClient()
init()

// Gama Server requests
function load_experiment() {
    return {
    "type": "load",
    "model": model_file,
    "experiment": experiment_name
    }
}

function reload_experiment() {
    return {
        "type":"reload",
        "exp_id":experiment_id
    }
}
function play_experiment() {
    return{
        "type": "play",
        "exp_id": experiment_id
    }
} 
function stop_experiment() {
    return{
        "type": "stop",
        "exp_id": experiment_id
    }
}
function pause_experiment() {
    return{
        "type": "pause",
        "exp_id": experiment_id
    }
}

function send_expression() {
    return  {
        "type": "expression",
        "content": "Send an expression", 
        "exp_id": experiment_id,
        "expr": current_expression
    }
}

function send_ask() {
    return {
        "type":"ask",
        "action": current_action,
        "agent": current_agent,
        "args": current_args
    }
}

function send_step() {
    return {
        "type": "step",
        "exp_id": experiment_id,
        "nb_step": current_nb_step
    }
}

function send_step_back() {
    return {
        "type": "stepBack",
        "exp_id": experiment_id,
        "nb_step": current_nb_step_back
    }
}

// Setters
function setConnectionState(newState) {
    connection_state = newState
    connection_html = document.querySelector("#connection")
    switch (connection_state) {
        case CONNECTED: connection_html.innerHTML = "CONNECTED"; connection_html.style = "color: green;"; break;
        case DISCONNECTED: connection_html.innerHTML = "DISCONNECTED"; connection_html.style = "color: red;"; break;
    }
}

function setGamaState(newState) {
    gama_state = newState
    state_html = document.querySelector("#gama-state")
    switch (gama_state) {
        case UNKNOWN: state_html.innerHTML = "UNKNOWN"; state_html.style = "color: white;"; break;
        case NONE: state_html.innerHTML = "NONE (no experiment running)"; state_html.style = "color: red;"; break;
        case NOTREADY: state_html.innerHTML = "NOTREADY (the experiment is not ready)"; state_html.style = "color: red;"; break;
        case PAUSED: state_html.innerHTML = "PAUSED (the experiment is paused)"; state_html.style = "color: orange;"; break;
        case RUNNING: state_html.innerHTML = "RUNNING (the experiment is running)"; state_html.style = "color: green;"; break;
    }
}

function createWebSocketClient() {
    let gama_full_address = 'ws://'+gama_ip_address+':'+gama_ws_port
    document.querySelector("#gama-address").innerHTML = gama_full_address;
    let gama_socket = new WebSocket(gama_full_address);

    gama_socket.onopen = function() {
        console.log("-> Connected to Gama Server");
        setConnectionState(CONNECTED)
        setGamaState(UNKNOWN)
    };

    gama_socket.onmessage = function(event) {
        try {
            const message = JSON.parse(event.data)
            switch (message.type) {
                case "SimulationStatus":
                    experiment_id = message.exp_id
                    switch (message.content) {
                        case NONE: setGamaState(NONE); break;
                        case NOTREADY: setGamaState(NOTREADY); break;
                        case PAUSED: setGamaState(PAUSED); break;
                        case RUNNING: setGamaState(RUNNING); break;
                        default: setGamaState(UNKNOWN); break;
                    }
                    break;
                default: break;
            }
            logRequestResponse(message, RESPONSE)
        }
        catch (exception) {
            logRequestResponse("An error occured when parsing the last message from Gama Server", RESPONSE)
        }
    }

    gama_socket.addEventListener('close', (event) => {
        setConnectionState(DISCONNECTED)
        setGamaState(UNKNOWN)
        if (event.wasClean) {
            console.log('-> The connection with Gama Server was properly be closed');
        } else {
            console.log('-> The connection with Gama Server interruped suddenly');
        }
    })
    gama_socket.addEventListener('error', (error) => {
        console.log("-> Failed to connect with Gama Server")
        
    });

    return gama_socket
}

function load() {
    if (connection_state == DISCONNECTED) {
        logRequestResponse("Gama Server is not connected", REQUEST)
        return
    }
    if ([NOTREADY, PAUSED, RUNNING].includes(gama_state) && model_file != undefined && experiment_name != undefined) {
        socket.send(JSON.stringify(reload_experiment()))
        logRequestResponse(reload_experiment(), REQUEST)
        return
    }
    if ([UNKNOWN, NONE].includes(gama_state) && model_file != undefined && experiment_name != undefined) {
        socket.send(JSON.stringify(load_experiment()))
        logRequestResponse(load_experiment(), REQUEST)
        return
    }
    else {
        logRequestResponse("Could not send Load or Reload since the model file or the experiment name is undefined", REQUEST)
    }
}

function start() {
    if (connection_state == DISCONNECTED) {
        logRequestResponse("Gama Server is not connected", REQUEST)
        return
    }
    if ([PAUSED].includes(gama_state)) {
        socket.send(JSON.stringify(play_experiment()))
        logRequestResponse(play_experiment(), REQUEST)
    }
    else {
        logRequestResponse("Could not send Play since Gama Server is not currently loaded nor paused", REQUEST)
    }
}

function pause() {
    if (connection_state == DISCONNECTED) {
        logRequestResponse("Gama Server is not connected", REQUEST)
        return
    }
    if ([RUNNING].includes(gama_state)) {
        socket.send(JSON.stringify(pause_experiment()))
        logRequestResponse(pause_experiment(), REQUEST)
    }
    else {
        logRequestResponse("Could not send Pause since Gama Server is not currently running",REQUEST)
    }
}

function end() {
    if (connection_state == DISCONNECTED) {
        logRequestResponse("Gama Server is not connected",REQUEST)
        return
    }
    if ([RUNNING, PAUSED].includes(gama_state)) {
        socket.send(JSON.stringify(stop_experiment()))
        logRequestResponse(stop_experiment(),REQUEST)
    }
    else {
        logRequestResponse("Could not send Stop since Gama Server is not currently running",REQUEST)
    }
}

function expression() {
    if (connection_state == DISCONNECTED) {
        logRequestResponse("Gama Server is not connected", REQUEST)
        return
    }
    if ([RUNNING, PAUSED].includes(gama_state)) {
        socket.send(JSON.stringify(send_expression()))
        logRequestResponse(send_expression(), REQUEST)
    }
    else {
        logRequestResponse("Could not send Expression since Gama Server is not currently running",REQUEST)
    }
}

function ask() {
    if (connection_state == DISCONNECTED) {
        logRequestResponse("Gama Server is not connected", REQUEST)
        return
    }
    if ([RUNNING, PAUSED].includes(gama_state)) {
        socket.send(JSON.stringify(send_ask()))
        logRequestResponse(send_ask(), REQUEST)
    }
    else {
        logRequestResponse("Could not send Ask since Gama Server is not currently running", REQUEST)
    }
}

function step() {
    if (connection_state == DISCONNECTED) {
        logRequestResponse("Gama Server is not connected", REQUEST)
        return
    }
    if ([RUNNING, PAUSED].includes(gama_state)) {
        socket.send(JSON.stringify(send_step()))
        logRequestResponse(send_step(), REQUEST)
    }
    else {
        logRequestResponse("Could not send Step since Gama Server is not currently running", REQUEST)
    }
}

function stepBack() {
    if (connection_state == DISCONNECTED) {
        logRequestResponse("Gama Server is not connected", REQUEST)
        return
    }
    if ([RUNNING, PAUSED].includes(gama_state)) {
        socket.send(JSON.stringify(send_step_back()))
        logRequestResponse(send_step_back(), REQUEST)
    }
    else {
        logRequestResponse("Could not send Step Back since Gama Server is not currently running", REQUEST)
    }
}


function setAddress() {
    gama_ip_address = document.querySelector("#ip-address-input").value
    gama_ws_port = document.querySelector("#ws-port-input").value 
    restartConnector()
}


function setExperiment() {
    experiment_name = document.querySelector("#experiment-name-input").value
    model_file = document.querySelector("#model-file-input").value 
}

function sendExpression() {
    current_expression = document.querySelector("#expression-input").value
    expression()
}

function sendAsk() {
    current_action = document.querySelector("#action-ask-input").value
    current_agent = document.querySelector("#agent-ask-input").value
    current_args = document.querySelector("#args-ask-input").value
    ask()
}

function sendStep() {
    current_nb_step = document.querySelector("#nb-step-input").value
    step()
}

function sendStepBack() {
    current_nb_step_back = document.querySelector("#nb-step-back-input").value
    stepBack()
}

function init() {
    document.querySelector("#ip-address-input").value  = gama_ip_address
    document.querySelector("#ws-port-input").value = gama_ws_port
}

function restartConnector() {
    socket = createWebSocketClient()
}

function logRequestResponse(message, type) {
    var title_message
    var content_message
    const reqres_div = document.createElement('div')
    const parent = document.querySelector("#request-response-timeline")
    parent.appendChild(reqres_div)
    parent.insertBefore(reqres_div, parent.firstChild);
    if (type==REQUEST) reqres_div.classList.add("request")
    else if (type==RESPONSE) reqres_div.classList.add("response")
    if (message.type != undefined) {
        var message_type = message.type
        switch (message_type) {
            case 'load': message_type = writeColored(message.type, 'lightblue'); break;
            case 'reload': message_type = writeColored(message.type, 'lightblue'); break;
            case 'play': message_type = writeColored(message.type, 'green'); break;
            case 'pause': message_type = writeColored(message.type, 'orange'); break;
            case 'stop': message_type = writeColored(message.type, 'red'); break;
            case 'CommandExecutedSuccessfully': message_type = writeColored(message.type, 'green'); break;
            case 'SimulationStatus': message_type = writeColored(message.type, 'lightblue'); break;
            case 'UnableToExecuteRequest': message_type = writeColored(message.type, 'red'); break;
            case 'GamaServerError': message_type = writeColored(message.type, 'red'); break;
            default : break;
        }
        if (type == REQUEST) title_message = "Sending message of type "+ message_type
        else if (type==RESPONSE) title_message = "Receiving message of type "+ message_type
        const title_div = document.createElement('div')
        reqres_div.appendChild(title_div)
        title_div.innerHTML = title_message
        content_message = '<pre>' + JSON.stringify(message, null, 2) + '</pre>'
        const content_div = document.createElement('div')
        reqres_div.appendChild(content_div)
        content_div.innerHTML = content_message
    }
    else {
        title_message = message
        const title_div = document.createElement('div')
        reqres_div.appendChild(title_div)
        title_div.innerHTML = title_message
        title_div.style = "color:red;"
    }
}

function writeColored(msg, color) {
    return '<span style="color:' + color + ';">'+ msg + '</span>'
}