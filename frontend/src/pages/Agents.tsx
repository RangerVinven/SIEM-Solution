import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { getAgents, getUnassignedAgents, assignAgent, deleteAgent, getUser, getSchool } from "@/lib/api"

type Agent = {
    id: string
    hostname: string
    ipAddress: string
    macAddress: string
    os: string
    status: string
    lastSeen: string
    location: string
    schoolId: string
    schoolName: string
}

export default function Agents() {
    const [agents, setAgents] = useState<Agent[]>([])
    const [unassignedAgents, setUnassignedAgents] = useState<Agent[]>([])
    const [loading, setLoading] = useState(true)
    const [assigning, setAssigning] = useState(false)
    const [school, setSchool] = useState<any>(null)
    const [user, setUser] = useState<any>(null)

    const [selectedAgentId, setSelectedAgentId] = useState("")
    const [location, setLocation] = useState("")

    useEffect(() => {
        fetchData()
    }, [])

    async function fetchData() {
        setLoading(true)
        try {
            const u = await getUser()
            setUser(u)
            if (u.schoolId) {
                const s = await getSchool(u.schoolId)
                setSchool(s)
            }
            const [a, ua] = await Promise.all([getAgents(), getUnassignedAgents()])
            setAgents(a)
            setUnassignedAgents(ua)
        } catch (e) {
            console.error(e)
        } finally {
            setLoading(false)
        }
    }

    async function handleAssign(e: React.FormEvent) {
        e.preventDefault()
        if (!selectedAgentId || !school || !user) return
        setAssigning(true)
        try {
            await assignAgent(selectedAgentId, user.schoolId, school.name, location || "Default")
            setLocation("")
            setSelectedAgentId("")
            await fetchData()
        } catch (e) {
            console.error(e)
        } finally {
            setAssigning(false)
        }
    }

    async function handleDelete(id: string) {
        if (!confirm("Are you sure you want to delete this agent?")) return
        try {
            await deleteAgent(id)
            await fetchData()
        } catch (e) {
            console.error(e)
        }
    }

    return (
        <div className="flex flex-col gap-6">
            <h1 className="text-2xl font-semibold">Agents</h1>

            <div className="rounded-md border p-4 space-y-4">
                <h2 className="text-lg font-medium">Assign New Agent</h2>
                {loading ? (
                    <p className="text-sm text-muted-foreground">Loading...</p>
                ) : unassignedAgents.length === 0 ? (
                    <p className="text-sm text-muted-foreground">No unassigned agents found.</p>
                ) : (
                    <form onSubmit={handleAssign} className="flex flex-col gap-4 sm:flex-row sm:items-end">
                        <div className="space-y-1 flex-1">
                            <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                                Select Agent
                            </label>
                            <select
                                className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                                value={selectedAgentId}
                                onChange={e => setSelectedAgentId(e.target.value)}
                                required
                            >
                                <option value="" disabled>Select an agent...</option>
                                {unassignedAgents.map(a => (
                                    <option key={a.id} value={a.id}>{a.hostname} ({a.ipAddress})</option>
                                ))}
                            </select>
                        </div>
                        <div className="space-y-1 flex-1">
                            <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                                Location
                            </label>
                            <Input
                                placeholder="e.g. Main Server Room"
                                value={location}
                                onChange={e => setLocation(e.target.value)}
                                required
                            />
                        </div>
                        <Button type="submit" disabled={assigning || !selectedAgentId || !location}>
                            {assigning ? "Assigning..." : "Assign Agent"}
                        </Button>
                    </form>
                )}
            </div>

            {loading ? (
                <div className="text-muted-foreground text-sm">Loading agents...</div>
            ) : agents.length === 0 ? (
                <div className="text-muted-foreground text-sm">No registered agents found.</div>
            ) : (
                <div className="overflow-x-auto rounded-md border">
                    <table className="w-full text-sm">
                        <thead>
                            <tr className="border-b bg-muted/50 text-left">
                                <th className="px-4 py-3 font-medium text-muted-foreground whitespace-nowrap">Hostname</th>
                                <th className="px-4 py-3 font-medium text-muted-foreground whitespace-nowrap">Location</th>
                                <th className="px-4 py-3 font-medium text-muted-foreground whitespace-nowrap">Status</th>
                                <th className="px-4 py-3 font-medium text-muted-foreground whitespace-nowrap">Last Seen</th>
                                <th className="px-4 py-3 font-medium text-muted-foreground whitespace-nowrap text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y">
                            {agents.map(agent => (
                                <tr key={agent.id} className="hover:bg-muted/30">
                                    <td className="px-4 py-3 whitespace-nowrap font-medium">{agent.hostname}</td>
                                    <td className="px-4 py-3 whitespace-nowrap">{agent.location || "-"}</td>
                                    <td className="px-4 py-3 whitespace-nowrap">
                                        <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold ${agent.status === 'ONLINE' ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400' : 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400'}`}>
                                            {agent.status}
                                        </span>
                                    </td>
                                    <td className="px-4 py-3 whitespace-nowrap text-muted-foreground">
                                        {agent.lastSeen ? new Date(agent.lastSeen).toLocaleString() : "-"}
                                    </td>
                                    <td className="px-4 py-3 whitespace-nowrap text-right">
                                        <Button variant="destructive" size="sm" className="text-white" onClick={() => handleDelete(agent.id)}>
                                            Delete
                                        </Button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    )
}
