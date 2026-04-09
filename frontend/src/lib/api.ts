const ACCOUNT_SERVICE = "http://localhost:8081/api/account"
const ALERT_SERVICE = "http://localhost:8084/alerts"
const LOG_SERVICE = "http://localhost:8083/logs"
const AGENT_SERVICE = "http://localhost:8086/agents"

export async function login(email: string, password: string) {
    const res = await fetch(`${ACCOUNT_SERVICE}/login`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
    })

    if (!res.ok) throw new Error(await res.text())
}

export async function register(firstName: string, lastName: string, email: string, password: string) {
    const res = await fetch(`${ACCOUNT_SERVICE}/users`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ firstName, lastName, email, password }),
    })

    if (!res.ok) throw new Error(await res.text())
}

export async function getUser() {
    const res = await fetch(ACCOUNT_SERVICE, {
        credentials: "include",
    })

    if (!res.ok) throw new Error(await res.text())
    return res.json()
}

export async function getAlertStats() {
    const res = await fetch(`${ALERT_SERVICE}/stats`, {
        credentials: "include",
    })

    if (!res.ok) throw new Error(await res.text())
    return res.json()
}

export async function getLogs(params?: { message?: string; hostname?: string; category?: string; level?: string; page?: number; size?: number }) {
    const query = new URLSearchParams()
    if (params?.message) query.set("message", params.message)
    if (params?.hostname) query.set("hostname", params.hostname)
    if (params?.category) query.set("category", params.category)
    if (params?.level) query.set("level", params.level)
    if (params?.page !== undefined) query.set("page", String(params.page))
    if (params?.size !== undefined) query.set("size", String(params.size))

    const res = await fetch(`${LOG_SERVICE}?${query}`, {
        credentials: "include",
    })

    if (!res.ok) throw new Error(await res.text())
    return res.json()
}

export async function getAlerts(params?: { resolved?: boolean; page?: number; size?: number }) {
    const query = new URLSearchParams()
    if (params?.resolved !== undefined) query.set("resolved", String(params.resolved))
    if (params?.page !== undefined) query.set("page", String(params.page))
    if (params?.size !== undefined) query.set("size", String(params.size))

    const res = await fetch(`${ALERT_SERVICE}?${query}`, {
        credentials: "include",
    })

    if (!res.ok) throw new Error(await res.text())
    return res.json()
}

export async function resolveAlert(id: number) {
    const res = await fetch(`${ALERT_SERVICE}/${id}/resolve`, {
        method: "PUT",
        credentials: "include",
    })

    if (!res.ok) throw new Error(await res.text())
}

export async function unresolveAlert(id: number) {
    const res = await fetch(`${ALERT_SERVICE}/${id}/unresolve`, {
        method: "PUT",
        credentials: "include",
    })

    if (!res.ok) throw new Error(await res.text())
}

export async function getSchool(id: string) {
    const res = await fetch(`${ACCOUNT_SERVICE}/schools/${id}`, {
        credentials: "include",
    })

    if (!res.ok) throw new Error(await res.text())
    return res.json()
}

export async function getLocations(schoolId: string) {
    const res = await fetch(`${ACCOUNT_SERVICE}/schools/${schoolId}/locations`, {
        credentials: "include",
    })

    if (!res.ok) throw new Error(await res.text())
    return res.json()
}

export async function getAgents() {
    const res = await fetch(AGENT_SERVICE, {
        credentials: "include",
    })

    if (!res.ok) throw new Error(await res.text())
    return res.json()
}

export async function getUnassignedAgents() {
    const res = await fetch(`${AGENT_SERVICE}/unassigned`, {
        credentials: "include",
    })

    if (!res.ok) throw new Error(await res.text())
    return res.json()
}

export async function assignAgent(id: string, schoolId: string, schoolName: string, location: string) {
    const query = new URLSearchParams({ schoolId, schoolName, location })
    const res = await fetch(`${AGENT_SERVICE}/${id}/assign?${query}`, {
        method: "PUT",
        credentials: "include",
    })

    if (!res.ok) throw new Error(await res.text())
    return res.json()
}

export async function deleteAgent(id: string) {
    const res = await fetch(`${AGENT_SERVICE}/${id}`, {
        method: "DELETE",
        credentials: "include",
    })

    if (!res.ok) throw new Error(await res.text())
}

export async function updateSchool(id: string, name: string) {
    const res = await fetch(`${ACCOUNT_SERVICE}/schools/${id}`, {
        method: "PUT",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name }),
    })

    if (!res.ok) throw new Error(await res.text())
    return res.json()
}

export async function addEmployee(schoolId: string, data: { firstName: string, lastName: string, email: string, password: string, role: string }) {
    const res = await fetch(`${ACCOUNT_SERVICE}/schools/${schoolId}/employees`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
    })

    if (!res.ok) throw new Error(await res.text())
    return res.json()
}

export async function getUsersBySchool(schoolId: string) {
    const res = await fetch(`${ACCOUNT_SERVICE}/schools/${schoolId}/users`, {
        credentials: "include",
    })

    if (!res.ok) throw new Error(await res.text())
    return res.json()
}

export async function createSchool(name: string) {
    const res = await fetch(`${ACCOUNT_SERVICE}/schools`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name }),
    })

    if (!res.ok) throw new Error(await res.text())
}
