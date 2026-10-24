const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const { test } = require("node:test");

const source = fs.readFileSync(path.join(__dirname,
    "../../models/Recipes/Using the Network Skill/scripts/connector/connector.js"), "utf8");

function connector() {
    const nodes = new Map();
    const element = () => ({
        value: "", innerHTML: "", children: [], classList: { add() {} },
        appendChild(child) { this.children.push(child); }, insertBefore() {}
    });
    const context = vm.createContext({
        console: { log() {} },
        document: {
            querySelector(id) {
                if (!nodes.has(id)) nodes.set(id, element());
                return nodes.get(id);
            },
            createElement: element
        },
        WebSocket: class {
            sent = [];
            addEventListener(type, listener) { this[type] = listener; }
            send(payload) { this.sent.push(JSON.parse(payload)); }
        }
    });
    vm.runInContext(source, context);
    context.socket.onopen();
    context.model_file = "/models/Boids.gaml";
    context.experiment_name = "Basic";
    context.receive = message => context.socket.onmessage({ data: JSON.stringify(message) });
    context.status = (content, exp_id = "exp-42") =>
        context.receive({ type: "SimulationStatus", content, exp_id });
    context.success = (type, content = "") =>
        context.receive({ type: "CommandExecutedSuccessfully", command: { type }, content });
    return context;
}

test("Stop completes in NONE despite late paused/running notifications", () => {
    for (const statusBeforeReply of [true, false]) {
        const c = connector();
        c.status("RUNNING");
        c.end();
        assert.equal(c.socket.sent.at(-1).type, "stop");
        assert.equal(c.gama_state, "NOTREADY");
        const sent = c.socket.sent.length;
        c.load();
        c.end();
        assert.equal(c.socket.sent.length, sent, "Do not overlap lifecycle commands");
        c.status("PAUSED");
        assert.equal(c.gama_state, "NOTREADY");
        if (statusBeforeReply) c.status("NONE");
        c.success("stop");
        if (!statusBeforeReply) c.status("NONE");
        c.status("PAUSED");
        c.status("RUNNING");
        assert.equal(c.gama_state, "NONE");
        assert.equal(c.experiment_id, undefined);
        c.load();
        assert.equal(c.socket.sent.at(-1).type, "load");
        c.status("NOTREADY");
        c.status("PAUSED");
        c.success("load", "exp-42");
        assert.equal(c.gama_state, "PAUSED");
        assert.equal(c.experiment_id, "exp-42");
        c.start();
        assert.equal(c.socket.sent.at(-1).type, "play");
        assert.equal(c.socket.sent.at(-1).exp_id, "exp-42");
    }
});

test("Reload preserves the experiment ID and observed running state", () => {
    const c = connector();
    c.status("RUNNING");
    c.load();
    assert.equal(c.socket.sent.at(-1).type, "reload");
    c.status("RUNNING");
    c.success("reload");
    assert.equal(c.experiment_id, "exp-42");
    assert.equal(c.gama_state, "RUNNING");
    c.pause();
    assert.equal(c.socket.sent.at(-1).exp_id, "exp-42");
});

test("Failed lifecycle commands release the client and permit a fresh load", () => {
    for (const type of ["MalformedRequest", "UnableToExecuteRequest", "RuntimeError", "GamaServerError"]) {
        const c = connector();
        c.status("PAUSED");
        c.end();
        c.receive({ type, content: "failure", command: { type: "stop" } });
        assert.equal(c.pending_command, undefined);
        assert.equal(c.gama_state, "UNKNOWN");
        c.load();
        assert.equal(c.socket.sent.at(-1).type, "load");
        c.receive({ type, content: "failure", command: { type: "load" } });
        c.load();
        assert.equal(c.socket.sent.at(-1).type, "load");
    }
});

test("Unrelated asynchronous errors do not release a pending Stop", () => {
    const c = connector();
    c.status("RUNNING");
    c.end();
    c.receive({ type: "GamaServerError", content: "An asynchronous simulation error" });
    c.receive({ type: "UnableToExecuteRequest", command: { type: "expression" }, content: "Invalid expression" });
    assert.equal(c.pending_command, "stop");
    assert.equal(c.gama_state, "NOTREADY");
    const sent = c.socket.sent.length;
    c.load();
    assert.equal(c.socket.sent.length, sent);
    c.success("stop");
    c.load();
    assert.equal(c.socket.sent.at(-1).type, "load");
});

test("Headless Stop also resets state without lifecycle notifications", () => {
    const c = connector();
    c.ignore_states = true;
    c.load();
    c.success("load", "headless-id");
    c.end();
    assert.equal(c.socket.sent.at(-1).exp_id, "headless-id");
    c.success("stop");
    assert.equal(c.gama_state, "NONE");
    assert.equal(c.experiment_id, undefined);
    c.load();
    assert.equal(c.socket.sent.at(-1).type, "load");
});

test("Only a new initialization can reopen a closed experiment", () => {
    const c = connector();
    c.status("PAUSED");
    c.status("NONE");
    c.status("PAUSED");
    assert.equal(c.gama_state, "NONE");
    c.status("NOTREADY", "another-experiment");
    c.status("PAUSED", "another-experiment");
    assert.equal(c.gama_state, "PAUSED");
    assert.equal(c.experiment_id, "another-experiment");
});

test("Repeated load/play/stop cycles do not require reconnecting", () => {
    const c = connector();
    for (let i = 0; i < 20; i++) {
        c.load();
        assert.equal(c.socket.sent.at(-1).type, "load");
        c.status("NOTREADY");
        c.status("PAUSED");
        c.success("load", "exp-42");
        c.start();
        c.status("RUNNING");
        c.end();
        c.status("NONE");
        c.success("stop");
        c.status("PAUSED");
        assert.equal(c.gama_state, "NONE");
    }
});
